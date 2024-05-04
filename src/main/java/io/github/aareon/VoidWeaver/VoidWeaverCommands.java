package io.github.aareon.VoidWeaver;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
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

    private static boolean isNamespaceBlacklisted(String namespace) {
        // Retrieve the blacklisted namespaces from the config JsonObject
        JsonArray blacklistedNamespaces = VoidWeaver.getBlacklistedNamespaces();

        // Check if the provided namespace is blacklisted
        return blacklistedNamespaces.contains(new JsonPrimitive(namespace));
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

                                    if (isNamespaceBlacklisted(dimensionNamespace)) {
                                        context.getSource().sendFeedback(() -> Text.literal("Namespace §6%s§r is blacklisted".formatted(dimensionNamespace)), false);
                                        return 1;
                                    }
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
            String sourceName = source.getDisplayName().getString();
            source.sendFeedback(() -> Text.translatable("voidweaver_works"), true);
            LOGGER.info("Test command executed successfully for {}", sourceName);
            return 0;
        } catch (Exception e) {
            LOGGER.error("Error executing test command", e);
            return 1; // Indicate command failure
        }
    }

    private static int createDimension(ServerCommandSource source, String dimensionNamespace, String dimensionName) {
        MinecraftServer server = source.getServer();
        RuntimeWorldConfig worldConfig = DimensionUtility.createStandardVoidConfig(server);
        Fantasy fantasy = Fantasy.get(server);

        RegistryKey<World> dimensionKey = DimensionUtility.getDimensionKey(dimensionNamespace, dimensionName);

        if (DimensionUtility.doesDimensionExist(server, dimensionKey)) {
            source.sendFeedback(() -> Text.translatable("dimension_already_exist", dimensionNamespace, dimensionName), true);
            return 1;
        }
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(dimensionNamespace, dimensionName), worldConfig);

        source.sendFeedback(() -> Text.translatable("created_dimension", dimensionNamespace, dimensionName), true);

        ServerWorld world = worldHandle.asWorld();
        BlockPos sourceBlockPos = BlockPos.ofFloored(source.getPosition());
        world.setBlockState(sourceBlockPos, Blocks.BEDROCK.getDefaultState());

        LOGGER.info("Placed bedrock at %s".formatted(sourceBlockPos));

        return 0;
    }

    private static int jumpToDimension(ServerCommandSource source, String dimensionNamespace, String dimensionName) {
        if (source.getPlayer() == null) {
            LOGGER.error(Text.translatable("server_cant_jump", dimensionNamespace, dimensionName).getString());
            return 1;
        }

        MinecraftServer server = source.getServer();
        String sourceName = source.getDisplayName().getString();

        Fantasy fantasy = Fantasy.get(server);

        RegistryKey<World> dimensionKey = DimensionUtility.getDimensionKey(dimensionNamespace, dimensionName);

        if (!DimensionUtility.doesDimensionExist(server, dimensionKey)) {
            source.sendFeedback(() -> Text.translatable("dimension_not_exist", dimensionNamespace, dimensionName), true);
            return 1;
        }

        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(
                DimensionUtility.getDimensionId(dimensionKey),
                DimensionUtility.createStandardVoidConfig(server));
        source.getPlayer().teleport(worldHandle.asWorld(), source.getPosition().getX(), source.getPosition().getY() + 1.5, source.getPosition().getZ(), 0, 0);

        LOGGER.info("Teleported player %s to %s,%s,%s".formatted(sourceName, source.getPosition().getX(), source.getPosition().getY(), source.getPosition().getZ()));
        source.sendFeedback(() -> Text.translatable("teleported_to_dimension", dimensionNamespace, dimensionName), true);

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

    public static RegistryKey<World> getDimensionKey(String dimensionNamespace, String dimensionName) {
        Identifier dimensionId = new Identifier(dimensionNamespace, dimensionName);
        return RegistryKey.of(RegistryKeys.WORLD, dimensionId);
    }

    public static Identifier getDimensionId(RegistryKey<World> dimensionKey) {
        return dimensionKey.getValue();
    }

    public static boolean doesDimensionExist(MinecraftServer server, RegistryKey<World> dimensionKey) {
        try {
            ServerWorld world = server.getWorld(dimensionKey);
            if (world != null) {
                return true; // Dimension exists
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // Dimension does not exist or other error occurred
            return false;
        }
        return false;
    }
}
