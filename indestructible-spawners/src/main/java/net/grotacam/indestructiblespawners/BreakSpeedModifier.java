package net.grotacam.indestructiblespawners;

import net.grotacam.indestructiblespawners.config.DifficultyHardness;
import net.grotacam.indestructiblespawners.config.IndestructibleSpawnersConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Applies the configured hardness to mining.
 *
 * <p>Hardness is never changed directly: vanilla clients would keep predicting mining with the
 * vanilla hardness and desync (blocks breaking then reappearing). Instead, while a player mines a
 * protected block, their {@code block_break_speed} attribute is scaled by
 * {@code vanillaHardness / configuredHardness}. Attributes are synced to the client, so the client
 * and the server compute exactly the same mining progress, cracks included.
 */
public final class BreakSpeedModifier {
	private static final Identifier ID = Identifier.fromNamespaceAndPath(IndestructibleSpawners.MOD_ID, "configured_hardness");

	/** Upper bound of the block_break_speed attribute: anything above already breaks instantly. */
	private static final double MAX_FACTOR = 1024.0;

	/**
	 * Called when the player starts mining a block. Sets the modifier for that block, or removes
	 * it if the block isn't protected.
	 *
	 * @return whether a modifier is now applied
	 */
	public static boolean update(ServerPlayer player, ServerLevel level, BlockPos pos) {
		AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
		if (attribute == null) {
			return false;
		}

		BlockState state = level.getBlockState(pos);
		DifficultyHardness settings = SpawnerProtection.settings(state);
		double factor = settings != null
				? factor(state.getDestroySpeed(level, pos), settings.hardness(level.getDifficulty()))
				: 1.0;
		return set(attribute, factor);
	}

	public static void clear(ServerPlayer player) {
		AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
		if (attribute != null) {
			attribute.removeModifier(ID);
		}
	}

	/** Mining speed multiplier that makes a block of {@code vanillaHardness} feel like {@code configuredHardness}. */
	private static double factor(float vanillaHardness, float configuredHardness) {
		if (configuredHardness == IndestructibleSpawnersConfig.INDESTRUCTIBLE) {
			return 0.0;
		}
		if (vanillaHardness <= 0.0F) {
			// Already unbreakable or instant because of another mod: leave it alone.
			return 1.0;
		}
		if (configuredHardness == 0.0F) {
			return MAX_FACTOR;
		}
		return Math.min(vanillaHardness / configuredHardness, MAX_FACTOR);
	}

	private static boolean set(AttributeInstance attribute, double factor) {
		// add_multiplied_total multiplies the final value by (1 + amount).
		double amount = factor - 1.0;
		AttributeModifier current = attribute.getModifier(ID);
		if (current != null) {
			if (current.amount() == amount) {
				return true;
			}
			attribute.removeModifier(ID);
		}
		if (factor == 1.0) {
			return false;
		}
		attribute.addTransientModifier(new AttributeModifier(ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		return true;
	}

	private BreakSpeedModifier() {
	}
}
