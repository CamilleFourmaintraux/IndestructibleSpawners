package net.grotacam.indestructiblespawners.client.config;

import java.util.Optional;
import java.util.function.Consumer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.grotacam.indestructiblespawners.IndestructibleSpawners;
import net.grotacam.indestructiblespawners.config.DifficultyHardness;
import net.grotacam.indestructiblespawners.config.IndestructibleSpawnersConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Cloth Config screen. Only referenced when Cloth Config is installed. */
public final class ConfigScreen {
	private static final String KEY = "config." + IndestructibleSpawners.MOD_ID + ".";

	public static Screen create(Screen parent) {
		IndestructibleSpawnersConfig config = IndestructibleSpawnersConfig.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable(KEY + "title"))
				.setSavingRunnable(IndestructibleSpawnersConfig::save);
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory hardness = builder.getOrCreateCategory(Component.translatable(KEY + "category.hardness"));

		hardness.addEntry(entries.startTextDescription(Component.translatable(KEY + "server_note")).build());
		hardness.addEntry(entries.startTextDescription(Component.translatable(KEY + "hardness_note")).build());

		// Vanilla's own block names, so they are translated in every language.
		hardness.addEntry(block(entries, "block.minecraft.spawner", "spawner", config.spawner));
		hardness.addEntry(block(entries, "block.minecraft.trial_spawner", "trial_spawner", config.trialSpawner));
		hardness.addEntry(block(entries, "block.minecraft.vault", "vault", config.vault));

		return builder.build();
	}

	private static SubCategoryListEntry block(ConfigEntryBuilder entries, String nameKey, String id, DifficultyHardness settings) {
		Component tooltip = Component.translatable(KEY + "hardness.tooltip." + id);
		SubCategoryBuilder category = entries.startSubCategory(Component.translatable(nameKey)).setExpanded(true);

		// Vanilla's own difficulty names, so they are translated in every language.
		category.add(field(entries, "options.difficulty.peaceful", tooltip, settings.peaceful, value -> settings.peaceful = value));
		category.add(field(entries, "options.difficulty.easy", tooltip, settings.easy, value -> settings.easy = value));
		category.add(field(entries, "options.difficulty.normal", tooltip, settings.normal, value -> settings.normal = value));
		category.add(field(entries, "options.difficulty.hard", tooltip, settings.hard, value -> settings.hard = value));
		return category.build();
	}

	private static FloatListEntry field(ConfigEntryBuilder entries, String nameKey, Component tooltip, float value, Consumer<Float> save) {
		return entries.startFloatField(Component.translatable(nameKey), value)
				.setDefaultValue(IndestructibleSpawnersConfig.DEFAULT_HARDNESS)
				.setMin(IndestructibleSpawnersConfig.INDESTRUCTIBLE)
				.setErrorSupplier(ConfigScreen::validate)
				.setTooltip(tooltip)
				.setSaveConsumer(save)
				.build();
	}

	private static Optional<Component> validate(Float value) {
		return IndestructibleSpawnersConfig.isValidHardness(value)
				? Optional.empty()
				: Optional.of(Component.translatable(KEY + "hardness.invalid"));
	}

	private ConfigScreen() {
	}
}
