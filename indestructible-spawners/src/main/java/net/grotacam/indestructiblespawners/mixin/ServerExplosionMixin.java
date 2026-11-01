package net.grotacam.indestructiblespawners.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.grotacam.indestructiblespawners.SpawnerProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Server-side guarantee, like the one for mining: whatever computed the list of blocks to
 * destroy (vanilla, an optimization mod, a mod with its own damage calculator), indestructible
 * blocks are removed from it before anything is broken.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@WrapOperation(
			method = "explode",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerExplosion;calculateExplodedPositions()Ljava/util/List;")
	)
	private List<BlockPos> indestructiblespawners$keepIndestructible(ServerExplosion explosion, Operation<List<BlockPos>> original) {
		List<BlockPos> positions = original.call(explosion);

		// Only copy the list when something has to be removed, which is rare.
		List<BlockPos> kept = null;
		for (int i = 0; i < positions.size(); i++) {
			BlockPos pos = positions.get(i);
			if (SpawnerProtection.isIndestructible(this.level, this.level.getBlockState(pos))) {
				if (kept == null) {
					kept = new ArrayList<>(positions.subList(0, i));
				}
			} else if (kept != null) {
				kept.add(pos);
			}
		}
		return kept != null ? kept : positions;
	}
}
