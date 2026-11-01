package net.grotacam.indestructiblespawners;

import net.fabricmc.api.ModInitializer;
import net.grotacam.indestructiblespawners.config.IndestructibleSpawnersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndestructibleSpawners implements ModInitializer {
	public static final String MOD_ID = "indestructiblespawners";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		IndestructibleSpawnersConfig.load();
	}
}
