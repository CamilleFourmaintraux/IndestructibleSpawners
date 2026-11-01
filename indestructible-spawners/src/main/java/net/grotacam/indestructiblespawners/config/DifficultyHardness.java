package net.grotacam.indestructiblespawners.config;

import net.grotacam.indestructiblespawners.IndestructibleSpawners;
import net.minecraft.world.Difficulty;

/**
 * Hardness of one block for each difficulty.
 *
 * <p>Fields are volatile because the config screen writes them on the client thread while the
 * integrated server reads them on its own thread.
 */
public final class DifficultyHardness {
	public volatile float peaceful = IndestructibleSpawnersConfig.DEFAULT_HARDNESS;
	public volatile float easy = IndestructibleSpawnersConfig.DEFAULT_HARDNESS;
	public volatile float normal = IndestructibleSpawnersConfig.DEFAULT_HARDNESS;
	public volatile float hard = IndestructibleSpawnersConfig.DEFAULT_HARDNESS;

	/** Hardcore worlds are locked to {@link Difficulty#HARD}, so they use the "hard" value. */
	public float hardness(Difficulty difficulty) {
		return switch (difficulty) {
			case PEACEFUL -> this.peaceful;
			case EASY -> this.easy;
			case NORMAL -> this.normal;
			case HARD -> this.hard;
		};
	}

	DifficultyHardness copy() {
		DifficultyHardness copy = new DifficultyHardness();
		copy.peaceful = this.peaceful;
		copy.easy = this.easy;
		copy.normal = this.normal;
		copy.hard = this.hard;
		return copy;
	}

	/** Resets invalid values to the default and returns whether anything changed. */
	boolean sanitize(String block) {
		boolean changed = false;
		if (!IndestructibleSpawnersConfig.isValidHardness(this.peaceful)) {
			this.peaceful = reset(block, "peaceful", this.peaceful);
			changed = true;
		}
		if (!IndestructibleSpawnersConfig.isValidHardness(this.easy)) {
			this.easy = reset(block, "easy", this.easy);
			changed = true;
		}
		if (!IndestructibleSpawnersConfig.isValidHardness(this.normal)) {
			this.normal = reset(block, "normal", this.normal);
			changed = true;
		}
		if (!IndestructibleSpawnersConfig.isValidHardness(this.hard)) {
			this.hard = reset(block, "hard", this.hard);
			changed = true;
		}
		return changed;
	}

	private static float reset(String block, String difficulty, float invalid) {
		IndestructibleSpawners.LOGGER.warn(
				"Invalid hardness {} for {}.{}: must be -1 (indestructible) or 0 and above. Using {} instead.",
				invalid, block, difficulty, IndestructibleSpawnersConfig.DEFAULT_HARDNESS);
		return IndestructibleSpawnersConfig.DEFAULT_HARDNESS;
	}
}
