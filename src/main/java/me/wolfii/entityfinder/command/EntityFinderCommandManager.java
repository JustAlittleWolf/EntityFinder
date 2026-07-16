package me.wolfii.entityfinder.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.wolfii.clientdatacommandselector.ClientEntityArgument;
import me.wolfii.clientdatacommandselector.ClientEntitySelector;
import me.wolfii.entityfinder.EntityFinder;
import me.wolfii.entityfinder.EntityFinderSettings;
import me.wolfii.entityfinder.mixin.KeyboardHandlerAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class EntityFinderCommandManager {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext ignoredBuildContext) {
        dispatcher.register(ClientCommands.literal("finder")
            .then(ClientCommands.literal("find")
                .then(ClientCommands.argument("entity_selector", ClientEntityArgument.entities())
                    .executes(context -> {
                        System.out.println("Finding entities...");
                        if (!EntityFinder.shouldRender) {
                            EntityFinder.shouldRender = true;
                            Minecraft.getInstance().debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.ALWAYS_ON);
                            ((KeyboardHandlerAccessor) Minecraft.getInstance().keyboardHandler).invokeDebugFeedbackTranslated("debug.show_hitboxes.on");
                        }
                        EntityFinder.highlighted.add((ClientEntitySelector) context.getArgument("entity_selector", EntitySelector.class));
                        return Command.SINGLE_SUCCESS;
                    })
                ))
            .then(ClientCommands.literal("hide")
                .then(ClientCommands.argument("entity_selector", ClientEntityArgument.entities())
                    .executes(context -> {
                        EntityFinder.hidden.add((ClientEntitySelector) context.getArgument("entity_selector", EntitySelector.class));
                        return Command.SINGLE_SUCCESS;
                    })
                ))
            .then(ClientCommands.literal("clear")
                .executes(_ -> {
                    EntityFinder.highlighted.clear();
                    EntityFinder.hidden.clear();
                    return Command.SINGLE_SUCCESS;
                }))
            .then(ClientCommands.literal("settings")
                .then(buildBooleanSettingCommand("render_tracers", value -> EntityFinderSettings.renderTracers = value))
                .then(buildBooleanSettingCommand("render_facing", value -> EntityFinderSettings.renderFacing = value))
                .then(buildBooleanSettingCommand("render_eye_height", value -> EntityFinderSettings.renderEyeHeight = value))
                .then(buildBooleanSettingCommand("hide_self", value -> EntityFinderSettings.hideSelf = value))
                .then(buildDoubleSettingCommand("min_distance", 0.0, 1024.0, value -> EntityFinderSettings.minimumDistanceSquared = value * value))
                .then(buildDoubleSettingCommand("max_distance", 0.0, 4096.0, value -> EntityFinderSettings.maximumDistanceSquared = value * value))
            )
            .then(ClientCommands.literal("count")
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal(EntityFinder.getHighlightedEntities().size() + " entities in range"));
                    return Command.SINGLE_SUCCESS;
                })
            )
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildBooleanSettingCommand(String name, Consumer<Boolean> onChange) {
        return ClientCommands.literal(name)
            .then(ClientCommands.literal("true")
                .executes(_ -> {
                    onChange.accept(true);
                    return Command.SINGLE_SUCCESS;
                })
            ).then(ClientCommands.literal("false")
                .executes(_ -> {
                    onChange.accept(false);
                    return Command.SINGLE_SUCCESS;
                })
            );
    }

    @SuppressWarnings("SameParameterValue")
    private static LiteralArgumentBuilder<FabricClientCommandSource> buildDoubleSettingCommand(String name, double min, double max, Consumer<Double> onChange) {
        return ClientCommands.literal(name)
            .then(ClientCommands.argument(name, DoubleArgumentType.doubleArg(min, max))
                .executes(context -> {
                    onChange.accept(context.getArgument(name, Double.class));
                    return Command.SINGLE_SUCCESS;
                })
            );
    }
}
