package io.github.aareon.VoidWeaver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class VoidWeaver implements ModInitializer {
	public static final String MODID = "voidweaver";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	private static final Gson GSON = new GsonBuilder().create();

	// Setting up config file stuff
	private static final String CONFIG_DIR = "config";
	private static final String MOD_CONFIG_DIR = MODID;
	private static final String CONFIG_FILE = "config.json";
	private static final Path CONFIG_PATH = Paths.get(CONFIG_DIR, MOD_CONFIG_DIR, CONFIG_FILE);

	// Define the config variable as a class variable
	private static JsonObject config; // This will hold the loaded configuration

	@Override
	public void onInitialize() {
        try {
            createConfigIfNotExists();
            config = readConfig();
        } catch (IOException e) {
            LOGGER.error("Error initializing config file: {}", e.getMessage());
        }

        ServerWorldEvents.LOAD.register(this::onWorldLoad);
        ServerEntityEvents.ENTITY_UNLOAD.register(this::onEntityUnload);

        VoidWeaverCommands.registerCommands();
        LOGGER.info("{} initialized.", MODID);
    }

	private void createConfigIfNotExists() throws IOException {
		Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_CONFIG_DIR);
		Files.createDirectories(configDir);

		Path configFile = configDir.resolve(CONFIG_FILE);
		if (!Files.exists(configFile)) {
			JsonObject defaultConfig = createDefaultConfig();
			try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
				GSON.toJson(defaultConfig, writer);
			}
		}
	}

	private JsonObject createDefaultConfig() {
		JsonObject defaultConfig = new JsonObject();
		// Create a JSON array to store the blacklisted namespaces
		JsonArray blacklistedNamespaces = new JsonArray();
		blacklistedNamespaces.add("minecraft");
		blacklistedNamespaces.add("forge");

		defaultConfig.add("blacklistedNamespaces", blacklistedNamespaces);

		return defaultConfig;
	}

	private JsonObject readConfig() throws IOException {
		createConfigIfNotExists();

		try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
			JsonObject configJson = JsonParser.parseReader(reader).getAsJsonObject();

			// Check if "blacklistedNamespaces" key exists in the config file
			if (configJson.has("blacklistedNamespaces")) {
				JsonArray blacklistedNamespaces = configJson.getAsJsonArray("blacklistedNamespaces");
				LOGGER.info("{} blacklisted namespaces: {}", MODID, blacklistedNamespaces);
			} else {
				LOGGER.warn("{} config file is missing 'blacklistedNamespaces' key", MODID);
			}
			return configJson;
		}
	}

	private void onEntityUnload(Entity entity, ServerWorld world) {
		if (entity instanceof ServerPlayerEntity player) {
			String reason = Objects.toString(player.getRemovalReason(), "unknown");
			LOGGER.info("Player {} removed for reason: {}", player.getName().getString(), reason);

			if ("KILLED".equals(reason) || "DISCARDED".equals(reason)) {
				LOGGER.info("Last known position: {}", player.getPos());
			}
		}
	}

	private void onWorldLoad(MinecraftServer server, ServerWorld world) {
		if (world.getRegistryKey() == World.OVERWORLD) {
			LOGGER.info("Overworld loaded on server {}", server.getName());
		}
	}

	static JsonArray getBlacklistedNamespaces() {
		// Initialize an empty JsonArray
		JsonArray blacklistedNamespaces = new JsonArray();

		// Check if the config JsonObject is not null and contains the "blacklistedNamespaces" key
		if (config != null && config.has("blacklistedNamespaces")) {
			// Retrieve the "blacklistedNamespaces" JsonArray from the config
			blacklistedNamespaces = config.getAsJsonArray("blacklistedNamespaces");
		} else {
			LOGGER.warn("{} config is null or missing 'blacklistedNamespaces' key", MODID);
		}

		// Return the blacklisted namespaces array
		return blacklistedNamespaces;
	}
}
