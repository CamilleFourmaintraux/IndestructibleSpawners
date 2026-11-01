package net.grotacam.indestructiblespawners.mixin;

import net.grotacam.indestructiblespawners.BreakSpeedModifier;
import net.grotacam.indestructiblespawners.SpawnerProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Player mining: configured hardness while mining, and no breaking at all when indestructible. */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
	@Shadow
	protected ServerLevel level;

	@Shadow
	@Final
	protected ServerPlayer player;

	@Shadow
	private boolean isDestroyingBlock;

	@Shadow
	private boolean hasDelayedDestroy;

	@Shadow
	private BlockPos delayedDestroyPos;

	@Shadow
	public abstract boolean isCreative();

	@Unique
	private boolean indestructiblespawners$speedModified;

	/**
	 * Every mining action starts with this packet, so it is the one place to pick the right
	 * mining speed. Running at HEAD means vanilla's own "instant break" check already sees it.
	 */
	@Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
	private void indestructiblespawners$applyHardness(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction,
			int maxBuildHeight, int sequence, CallbackInfo ci) {
		if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && !this.isCreative()) {
			this.indestructiblespawners$speedModified = BreakSpeedModifier.update(this.player, this.level, pos);
		}
	}

	/**
	 * When a client reports a block broken before the server agrees, vanilla keeps mining it
	 * server-side and breaks it later, at whatever speed the player has at that time. Our
	 * modifier is gone by then, so that would ignore the configured hardness: for protected
	 * blocks, the client simply has to mine again.
	 */
	@Inject(method = "handleBlockBreakAction", at = @At("RETURN"))
	private void indestructiblespawners$cancelDelayedDestroy(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction,
			int maxBuildHeight, int sequence, CallbackInfo ci) {
		if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK
				&& this.hasDelayedDestroy
				&& pos.equals(this.delayedDestroyPos)
				&& SpawnerProtection.isProtected(this.level.getBlockState(pos))) {
			this.hasDelayedDestroy = false;
		}
	}

	/** Removes the modifier as soon as the player is no longer mining (stopped, aborted, block gone...). */
	@Inject(method = "tick", at = @At("RETURN"))
	private void indestructiblespawners$clearWhenIdle(CallbackInfo ci) {
		if (this.indestructiblespawners$speedModified && !this.isDestroyingBlock) {
			BreakSpeedModifier.clear(this.player);
			this.indestructiblespawners$speedModified = false;
		}
	}

	/**
	 * Server-side guarantee, independent of the client: an indestructible block is never broken
	 * by a player outside creative, whatever the client sends or whatever other mod calls this.
	 * Vanilla resends the block to the client when this returns false.
	 */
	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	private void indestructiblespawners$preventDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!this.isCreative() && SpawnerProtection.isIndestructible(this.level, this.level.getBlockState(pos))) {
			cir.setReturnValue(false);
		}
	}
}
