package io.github.aareon.VoidWeaver;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;
import net.minecraft.registry.RegistryKeys;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VoidWeaverCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger("voidweaver");

    public static void registerCommands() {
        // Command registration
        LOGGER.info("Registering commands for VoidWeaver");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("voidweaver")
                .then(registerTestCommand())
                .then(registerNewDimensionCommand())
                .then(registerJumpCommand())));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> registerTestCommand() {
        return literal("test")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> testMod(context.getSource()));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> registerNewDimensionCommand() {
        return literal("new")
                .then(argument("dimensionNamespace", StringArgumentType.string())
                        .then(argument("dimensionName", StringArgumentType.string())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    String dimensionNamespace = StringArgumentType.getString(context, "dimensionNamespace");
                                    String dimensionName = StringArgumentType.getString(context, "dimensionName");
                                    return createDimension(context.getSource(), dimensionNamespace, dimensionName);
                                })));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> registerJumpCommand() {
        return literal("jump")
                .then(argument("dimensionNamespace", StringArgumentType.string())
                        .then(argument("dimensionName", StringArgumentType.string())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    String dimensionNamespace = StringArgumentType.getString(context, "dimensionNamespace");
                                    String dimensionName = StringArgumentType.getString(context, "dimensionName");
                                    return jumpToDimension(context.getSource(), dimensionNamespace, dimensionName);
                                })));
    }

    private static int testMod(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = Objects.requireNonNull(source.getPlayer());
            source.sendFeedback(() -> Text.literal("@%s §cVoidweaver§r is working".formatted(player.getName().getString())), true);
            LOGGER.info("Test command executed successfully for {}", player.getName().getString());
            return 0;
        } catch (Exception e) {
            LOGGER.error("Error executing test command", e);
            return 1; // Indicate command failure
        }
    }

    private static int createDimension(ServerCommandSource source, String namespace, String name) {
        MinecraftServer server = source.getServer();
        RuntimeWorldConfig worldConfig = DimensionUtility.createStandardVoidConfig(server);
        Fantasy fantasy = Fantasy.get(server);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(namespace, name), worldConfig);

        source.sendFeedback(() -> Text.literal("Created new dimension: §6%s§r:§c%s§r".formatted(namespace, name)), true);
        ServerPlayerEntity player = Objects.requireNonNull(source.getPlayer());

        ServerWorld world = worldHandle.asWorld();
        world.setBlockState(player.getBlockPos(), Blocks.BEDROCK.getDefaultState());
        LOGGER.info("Placed bedrock at %s".formatted(player.getBlockPos()));
        return 0;
    }

    private static int jumpToDimension(ServerCommandSource source, String namespace, String name) {
        MinecraftServer server = source.getServer();
        ServerPlayerEntity player = Objects.requireNonNull(source.getPlayer());
        Fantasy fantasy = Fantasy.get(server);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(namespace, name), DimensionUtility.createStandardVoidConfig(server));
        player.teleport(worldHandle.asWorld(), player.getX(), player.getY() + 1.5, player.getZ(), player.getYaw(), player.getPitch());
        LOGGER.info("Teleported player %s to %s,%s,%s".formatted(player.getName().getString(), player.getX(), player.getY(), player.getZ()));
        source.sendFeedback(() -> Text.literal("Teleported to dimension: §6%s§r:§c%s§r".formatted(namespace, name)), true);
        return 0;
    }
}

class DimensionUtility {

    public static RuntimeWorldConfig createStandardVoidConfig(MinecraftServer server) {
        return new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setDifficulty(Difficulty.NORMAL)
                .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, true)
                .setGenerator(new VoidChunkGenerator(server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(0).get()))
                .setSeed(1234L);
    }
}
