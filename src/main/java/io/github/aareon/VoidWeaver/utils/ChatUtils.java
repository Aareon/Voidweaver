package io.github.aareon.VoidWeaver.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class ChatUtils {

    public static void broadcast(MinecraftServer server, Text message) {
        // Get all players in the server
        Iterable<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        // Broadcast message to each player
        for (ServerPlayerEntity player : players) {
            player.sendMessage(message, false);
        }
    }

    public static void broadcast(World world, Text message) {
        // Get the MinecraftServer instance from the world
        MinecraftServer server = world.getServer();
        if (server != null) {
            broadcast(server, message);
        }
    }

    // Overloaded method to broadcast a String message
    public static void broadcast(MinecraftServer server, String message) {
        broadcast(server, Text.literal(message));
    }

    public static void broadcast(World world, String message) {
        broadcast(world, Text.literal(message));
    }
}