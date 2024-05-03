package io.github.aareon.VoidWeaver;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VoidWeaverCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("voidweaver")
                .then(literal("test")
                        .requires(source -> source.hasPermissionLevel(2))
                        // Command to test if mod is working
                        .executes(context -> {
                            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
                            String playerName = player.getName().getLiteralString();


                            try {
                                context.getSource().sendFeedback(() ->
                                        Text.literal("@%s §cVoidweaver§r is working".formatted(playerName)
                                        ), true);
                                //player.sendMessage(Text.literal("VoidWeaver is working!"), true);
                            } catch (Exception e) {
                                context.getSource().sendFeedback(() ->
                                        Text.literal("Something went wrong with §cVoidweaver§r @%s".formatted(playerName)
                                        ), true);
                                //player.sendMessage(Text.literal("Something is wrong with VoidWeaver!").formatted(Formatting.RED, Formatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        }))

                .then(literal("new")
                        .then(argument("dimensionNamespace", StringArgumentType.string())
                        .then(argument("dimensionName", StringArgumentType.string())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    final String dimensionNamespace = StringArgumentType.getString(context, "dimensionNamespace");
                                    final String dimensionName = StringArgumentType.getString(context, "dimensionName");

                                    MinecraftServer server = context.getSource().getServer();
                                    ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

                                    Fantasy fantasy = Fantasy.get(context.getSource().getServer());

                                    RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                                            .setDimensionType(DimensionTypes.OVERWORLD)
                                            .setDifficulty(Difficulty.NORMAL)
                                            .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, true)
                                            .setGenerator(new VoidChunkGenerator(server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(0).get()))
                                            .setSeed(1234L);

                                    RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(dimensionNamespace, dimensionName), worldConfig);
                                    ServerWorld world = worldHandle.asWorld();
                                    world.setBlockState(player.getBlockPos(), Blocks.BEDROCK.getDefaultState());

                                    context.getSource().sendFeedback(() -> Text.literal("Created worldConfig for §c%s§r dimension".formatted(dimensionName)), true);
                                    return 0;
                                }))))

                .then(literal("jump")
                        .then(argument("dimensionNamespace", StringArgumentType.string())
                        .then(argument("dimensionName", StringArgumentType.string())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    final String dimensionNamespace = StringArgumentType.getString(context, "dimensionNamespace");
                                    final String dimensionName = StringArgumentType.getString(context, "dimensionName");

                                    context.getSource().sendFeedback(() -> Text.literal("Jumping to §c%s:%s§r dimension".formatted(dimensionNamespace, dimensionName)), true);

                                    // Teleport player to dimension by name using Fantasy
                                    ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
                                    MinecraftServer server = context.getSource().getServer();
                                    Fantasy fantasy = Fantasy.get(server);

                                    // Get the dimension to teleport to
                                    RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(dimensionNamespace, dimensionName), new RuntimeWorldConfig()
                                            .setDimensionType(DimensionTypes.OVERWORLD)
                                            .setDifficulty(Difficulty.NORMAL)
                                            .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, true)
                                            .setGenerator(new VoidChunkGenerator(server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(0).get()))
                                            .setSeed(1234L)
                                    );

                                    // Teleport player to dimension
                                    player.teleport(worldHandle.asWorld(), player.getX(), player.getY() + 1.5, player.getZ(), player.getYaw(), player.getPitch());

                                    return 0;
                                }))))));
    }
}
