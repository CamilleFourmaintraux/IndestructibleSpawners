package net.grotacam.indestructiblespawners.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.grotacam.indestructiblespawners.SpawnerProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Blocks the Wither breaks by moving into them (after taking damage). Its skull explosions are
 * handled by {@link ServerExplosionMixin}.
 *
 * <p>Vanilla uses the #minecraft:wither_immune tag here, but a tag can't depend on the
 * difficulty, hence the wrap.
 */
@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
	@WrapOperation(
			method = "customServerAiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;canDestroy(Lnet/minecraft/world/level/block/state/BlockState;)Z"
			)
	)
	private boolean indestructiblespawners$protect(BlockState state, Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level) {
		return original.call(state) && !SpawnerProtection.isIndestructible(level, state);
	}
}
