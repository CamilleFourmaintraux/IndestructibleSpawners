package net.grotacam.indestructiblespawners.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.grotacam.indestructiblespawners.SpawnerProtection;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Blue Wither skulls cap the resistance of every block the Wither can destroy at 0.8, which
 * would undo {@link ExplosionDamageCalculatorMixin}. Indestructible blocks are excluded from that.
 */
@Mixin(WitherSkull.class)
public abstract class WitherSkullMixin {
	@WrapOperation(
			method = "getBlockExplosionResistance",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;canDestroy(Lnet/minecraft/world/level/block/state/BlockState;)Z"
			)
	)
	private boolean indestructiblespawners$protect(BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter blockGetter) {
		return original.call(state)
				&& !(blockGetter instanceof LevelAccessor level && SpawnerProtection.isIndestructible(level, state));
	}
}
