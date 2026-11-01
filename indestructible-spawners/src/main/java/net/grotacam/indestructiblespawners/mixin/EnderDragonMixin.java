package net.grotacam.indestructiblespawners.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.grotacam.indestructiblespawners.SpawnerProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Blocks the Ender Dragon destroys by flying through them. Indestructible blocks are treated as
 * part of #minecraft:dragon_immune (a tag can't depend on the difficulty, hence the wrap).
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {
	@WrapOperation(
			method = "checkWalls",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"
			)
	)
	private boolean indestructiblespawners$protect(BlockState state, TagKey<Block> tag, Operation<Boolean> original,
			@Local(argsOnly = true) ServerLevel level) {
		if (BlockTags.DRAGON_IMMUNE.equals(tag) && SpawnerProtection.isIndestructible(level, state)) {
			return true;
		}
		return original.call(state, tag);
	}
}
