package me.wolfii.entityfinder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.noeul.fabricmod.clientdatacommand.ClientEntityArgumentType;
import dev.noeul.fabricmod.clientdatacommand.ClientEntitySelector;
import me.wolfii.entityfinder.EntityFinderSettings;
import me.wolfii.entityfinder.client.EntityFinder;
import me.wolfii.entityfinder.client.EntityFinderFeedback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;

import java.util.function.Consumer;

public class EntityFinderCommandManager {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess ignoredRegistryAccess) {
        dispatcher.register(ClientCommandManager.literal("finder")
                .then(ClientCommandManager.literal("find")
                        .then(ClientCommandManager.argument("entity_selector", ((ClientEntityArgumentType) EntityArgumentType.entities()).clientDataCommand$withAlwaysAllowAtSelectors())
                                .executes(context -> {
                                    if (!EntityFinder.shouldRender) {
                                        EntityFinder.shouldRender = true;
                                        MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(true);
                                        EntityFinderFeedback.sendDebugMessage("debug.show_hitboxes.on");
                                    }
                                    EntityFinder.highlighted.add((ClientEntitySelector) context.getArgument("entity_selector", EntitySelector.class));
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("hide")
                        .then(ClientCommandManager.argument("entity_selector", ((ClientEntityArgumentType) EntityArgumentType.entities()).clientDataCommand$withAlwaysAllowAtSelectors())
                                .executes(context -> {
                                    EntityFinder.hidden.add((ClientEntitySelector) context.getArgument("entity_selector", EntitySelector.class));
                                    return 1;
                                })
                        ))
                .then(ClientCommandManager.literal("clear")
                        .executes(context -> {
                            EntityFinder.highlighted.clear();
                            EntityFinder.hidden.clear();
                            return 1;
                        }))
                .then(ClientCommandManager.literal("settings")
                        .then(buildBooleanSettingCommand("render_tracers", value -> EntityFinderSettings.renderTracers = value))
                        .then(buildBooleanSettingCommand("render_facing", value -> EntityFinderSettings.renderFacing = value))
                        .then(buildBooleanSettingCommand("render_eye_height", value -> EntityFinderSettings.renderEyeHeight = value))
                        .then(buildBooleanSettingCommand("hide_self", value -> EntityFinderSettings.hideSelf = value))
                        .then(buildDoubleSettingCommand("min_distance", 0.0, 1024.0, value -> EntityFinderSettings.minimumDistanceSquared = value * value))
                        .then(buildDoubleSettingCommand("max_distance", 0.0, 4096.0, value -> EntityFinderSettings.maximumDistanceSquared = value * value))
                )
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildBooleanSettingCommand(String name, Consumer<Boolean> onChange) {
        return ClientCommandManager.literal(name)
                .then(ClientCommandManager.literal("true")
                        .executes(context -> {
                            onChange.accept(true);
                            return 1;
                        })
                ).then(ClientCommandManager.literal("false")
                        .executes(context -> {
                            onChange.accept(false);
                            return 1;
                        })
                );
    }

    @SuppressWarnings("SameParameterValue")
    private static LiteralArgumentBuilder<FabricClientCommandSource> buildDoubleSettingCommand(String name, double min, double max, Consumer<Double> onChange) {
        return ClientCommandManager.literal(name)
                .then(ClientCommandManager.argument(name, DoubleArgumentType.doubleArg(min, max))
                        .executes(context -> {
                            onChange.accept(context.getArgument(name, Double.class));
                            return 1;
                        })
                );
    }
}
