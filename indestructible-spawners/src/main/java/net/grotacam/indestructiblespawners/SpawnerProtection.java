package net.grotacam.indestructiblespawners;

import net.grotacam.indestructiblespawners.config.DifficultyHardness;
import net.grotacam.indestructiblespawners.config.IndestructibleSpawnersConfig;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Which blocks the mod protects, and how hard they are in the current difficulty. */
public final class SpawnerProtection {
	/** Per-difficulty settings of the block, or null if the mod doesn't protect it. */
	@Nullable
	public static DifficultyHardness settings(BlockState state) {
		IndestructibleSpawnersConfig config = IndestructibleSpawnersConfig.get();
		if (state.is(Blocks.SPAWNER)) {
			return config.spawner;
		}
		if (state.is(Blocks.TRIAL_SPAWNER)) {
			return config.trialSpawner;
		}
		if (state.is(Blocks.VAULT)) {
			return config.vault;
		}
		return null;
	}

	public static boolean isProtected(BlockState state) {
		return state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER) || state.is(Blocks.VAULT);
	}

	/** Whether the block should currently behave like bedrock. */
	public static boolean isIndestructible(LevelAccessor level, BlockState state) {
		// Cheap block check first: explosions call this for every block on every ray.
		DifficultyHardness settings = settings(state);
		return settings != null && settings.hardness(level.getDifficulty()) == IndestructibleSpawnersConfig.INDESTRUCTIBLE;
	}

	private SpawnerProtection() {
	}
}
