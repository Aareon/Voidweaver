package io.github.aareon.VoidWeaver;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class VoidWeaver implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "voidweaver";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static MinecraftServer Server;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerWorldEvents.LOAD.register(this::onWorldLoad);
		ServerEntityEvents.ENTITY_UNLOAD.register(this::onPlayerUnload);

		VoidWeaverCommands.registerCommands();
		LOGGER.info("{} says hi!", MODID);
	}

	private void onPlayerUnload(Entity entity, ServerWorld serverWorld) {
		if (entity instanceof ServerPlayerEntity player) {
			LOGGER.info(String.valueOf(Objects.requireNonNull(entity.getRemovalReason()).toString()));
			if (player.getRemovalReason() != null && (Objects.equals(player.getRemovalReason().toString(), "KILLED") || Objects.equals(player.getRemovalReason().toString(), "DISCARDED"))) {
				try {
					// /back command position
					LOGGER.info(player.getPos().toString());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void onWorldLoad(MinecraftServer minecraftServer, ServerWorld serverWorld) {
		// Ensure this only runs once
		if (serverWorld.getRegistryKey() == World.OVERWORLD) {
			Server = minecraftServer;
		}
	}
}