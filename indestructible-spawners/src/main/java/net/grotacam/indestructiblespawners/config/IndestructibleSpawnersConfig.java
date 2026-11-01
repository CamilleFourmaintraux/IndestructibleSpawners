package net.grotacam.indestructiblespawners.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.grotacam.indestructiblespawners.IndestructibleSpawners;

/**
 * Hardness of each protected block for each difficulty, stored in
 * config/indestructiblespawners.json.
 *
 * <p>A value of {@link #INDESTRUCTIBLE} (-1) makes the block behave like bedrock in that
 * difficulty. Any value of 0 or more is used as the block's hardness instead, on the same scale
 * as vanilla (spawner 5, trial spawner 50, vault 50, obsidian 50).
 */
public final class IndestructibleSpawnersConfig {
	public static final float INDESTRUCTIBLE = -1.0F;
	public static final float DEFAULT_HARDNESS = INDESTRUCTIBLE;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(IndestructibleSpawners.MOD_ID + ".json");

	private static volatile IndestructibleSpawnersConfig instance = new IndestructibleSpawnersConfig();

	public DifficultyHardness spawner = new DifficultyHardness();
	@SerializedName("trial_spawner")
	public DifficultyHardness trialSpawner = new DifficultyHardness();
	public DifficultyHardness vault = new DifficultyHardness();

	public static IndestructibleSpawnersConfig get() {
		return instance;
	}

	public static boolean isValidHardness(float hardness) {
		return hardness == INDESTRUCTIBLE || (hardness >= 0.0F && Float.isFinite(hardness));
	}

	public static void load() {
		IndestructibleSpawnersConfig loaded = new IndestructibleSpawnersConfig();
		boolean write = true;

		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				JsonElement json = JsonParser.parseReader(reader);
				IndestructibleSpawnersConfig read = GSON.fromJson(json, IndestructibleSpawnersConfig.class);
				if (read != null) {
					loaded = read;
					// Only rewrite an existing file when it had to be migrated or fixed.
					boolean migrated = json.isJsonObject() && loaded.migrateFromSingleValues(json.getAsJsonObject());
					write = loaded.sanitize() | migrated;
				}
			} catch (IOException | JsonParseException | IllegalStateException e) {
				// Keep the broken file so the user can fix it instead of losing their edits.
				IndestructibleSpawners.LOGGER.error("Could not read {}, using default values until it is fixed", PATH, e);
				write = false;
			}
		}

		instance = loaded;
		if (write) {
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(instance));
		} catch (IOException e) {
			IndestructibleSpawners.LOGGER.error("Could not save {}", PATH, e);
		}
	}

	/**
	 * Version 1.0.0 had a single set of values for all blocks ({"peaceful": ..., "easy": ...}).
	 * Those values are copied to every block so existing servers keep their behavior.
	 */
	private boolean migrateFromSingleValues(JsonObject json) {
		if (!json.has("peaceful") && !json.has("easy") && !json.has("normal") && !json.has("hard")) {
			return false;
		}
		DifficultyHardness shared = GSON.fromJson(json, DifficultyHardness.class);
		this.spawner = shared.copy();
		this.trialSpawner = shared.copy();
		this.vault = shared.copy();
		IndestructibleSpawners.LOGGER.info("Migrated {} to per-block values", PATH.getFileName());
		return true;
	}

	/** Restores missing blocks, resets invalid values and returns whether anything changed. */
	private boolean sanitize() {
		boolean changed = false;
		if (this.spawner == null) {
			this.spawner = new DifficultyHardness();
			changed = true;
		}
		if (this.trialSpawner == null) {
			this.trialSpawner = new DifficultyHardness();
			changed = true;
		}
		if (this.vault == null) {
			this.vault = new DifficultyHardness();
			changed = true;
		}
		changed |= this.spawner.sanitize("spawner");
		changed |= this.trialSpawner.sanitize("trial_spawner");
		changed |= this.vault.sanitize("vault");
		return changed;
	}
}
