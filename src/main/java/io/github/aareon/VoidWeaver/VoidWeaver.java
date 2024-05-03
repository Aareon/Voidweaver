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
	public static final String MODID = "voidweaver";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		ServerWorldEvents.LOAD.register(this::onWorldLoad);
		ServerEntityEvents.ENTITY_UNLOAD.register(this::onEntityUnload);

		VoidWeaverCommands.registerCommands();
		LOGGER.info("{} initialized.", MODID);
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
}
