package net.grotacam.indestructiblespawners.mixin;

import java.util.Optional;
import net.grotacam.indestructiblespawners.SpawnerProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Indestructible blocks get bedrock's blast resistance, so like bedrock they also shield the
 * blocks behind them.
 *
 * <p>This is where every explosion asks for a block's resistance: vanilla's ray casting and
 * optimized replacements such as Lithium's alike. The vanilla subclasses (entity-based, simple,
 * respawn anchor) all fall back to this method through {@code super}.
 */
@Mixin(ExplosionDamageCalculator.class)
public abstract class ExplosionDamageCalculatorMixin {
	@Inject(method = "getBlockExplosionResistance", at = @At("HEAD"), cancellable = true)
	private void indestructiblespawners$bedrockResistance(Explosion explosion, BlockGetter blockGetter, BlockPos pos, BlockState state,
			FluidState fluidState, CallbackInfoReturnable<Optional<Float>> cir) {
		if (blockGetter instanceof LevelAccessor level && SpawnerProtection.isIndestructible(level, state)) {
			cir.setReturnValue(Optional.of(Blocks.BEDROCK.getExplosionResistance()));
		}
	}
}
