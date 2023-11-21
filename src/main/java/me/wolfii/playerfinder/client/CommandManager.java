package me.wolfii.playerfinder.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.render.Rendermode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.mojang.brigadier.arguments.StringArgumentType;

public class CommandManager {
    private static final MutableText prefix = Text.literal("[PlayerFinder] ").formatted(Formatting.GRAY);

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess ignoredRegistryAccess) {
        dispatcher.register(ClientCommandManager.literal("find")
                .then(ClientCommandManager.argument("playername", StringArgumentType.greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            PlayerFinder.hightLightAll = false;
                            if (PlayerFinder.rendermode == Rendermode.NONE) {
                                PlayerFinder.rendermode = PlayerFinder.lastRendermode;
                                MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(true);
                                DebugMessage.debugLog("debug.show_hitboxes.on");
                            }
                            String playerName = context.getArgument("playername", String.class);
                            if (PlayerFinder.highlightedPlayers.stream().noneMatch(playerName::equalsIgnoreCase)) {
                                PlayerFinder.highlightedPlayers.add(playerName);
                            }
                            sendInfoText(Text.literal(String.format(Text.translatable("playerfinder.find.specific").getString(), playerName)));
                            return 1;
                        })));
        dispatcher.register(ClientCommandManager.literal("findall")
                .executes(context -> {
                    PlayerFinder.hightLightAll = true;
                    if (PlayerFinder.rendermode == Rendermode.NONE) {
                        PlayerFinder.rendermode = PlayerFinder.lastRendermode;
                        MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(true);
                        DebugMessage.debugLog("debug.show_hitboxes.on");
                    }
                    PlayerFinder.highlightedPlayers.clear();
                    sendInfoText(Text.translatable("playerfinder.find.all"));
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("findnone")
                .executes(context -> {
                    PlayerFinder.hightLightAll = false;
                    if (PlayerFinder.rendermode != Rendermode.NONE) {
                        PlayerFinder.rendermode = Rendermode.NONE;
                        MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(false);
                        DebugMessage.debugLog("debug.show_hitboxes.off");
                    }
                    PlayerFinder.highlightedPlayers.clear();
                    sendInfoText(Text.translatable("playerfinder.find.none"));
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("findlist")
                .executes(context -> {
                    MutableText message = Text.translatable("playerfinder.find.highlighting").formatted(Formatting.WHITE);
                    if (PlayerFinder.hightLightAll) {
                        message.append(Text.translatable("playerfinder.find.everyone").formatted(Formatting.BOLD).formatted(Formatting.WHITE));
                    } else if (PlayerFinder.highlightedPlayers.isEmpty()) {
                        message.append(Text.translatable("playerfinder.find.nobody").formatted(Formatting.BOLD).formatted(Formatting.WHITE));
                    } else {
                        message.append(Text.literal(String.join(", ", PlayerFinder.highlightedPlayers)).formatted(Formatting.WHITE));
                    }
                    sendInfoMessage(message);
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("unfind")
                .then(ClientCommandManager.argument("playername", StringArgumentType.greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            String playerName = context.getArgument("playername", String.class);
                            PlayerFinder.highlightedPlayers.removeIf(playerName::equalsIgnoreCase);
                            sendInfoText(Text.literal(String.format(Text.translatable("playerfinder.unfind.specific").getString(), playerName)));
                            return 1;
                        })));
        dispatcher.register(ClientCommandManager.literal("finddistance")
                .then(ClientCommandManager.argument("minimum", DoubleArgumentType.doubleArg(0.0, 1024.0))
                        .executes(context -> {
                            double minDistance = context.getArgument("minimum", Double.class);
                            Config.minimumDistanceSquared = Math.max(Math.min(minDistance * minDistance, 128 * 128), 0.0);
                            sendInfoText(Text.literal(String.format(Text.translatable("playerfinder.finddistance.min").getString(), minDistance)));
                            return 1;
                        })
                        .then(ClientCommandManager.argument("maximum", DoubleArgumentType.doubleArg(0.0, 4096.0))
                                .executes(context -> {
                                    double minDistance = context.getArgument("minimum", Double.class);
                                    Config.minimumDistanceSquared = minDistance * minDistance;
                                    double maxDistance = context.getArgument("maximum", Double.class);
                                    Config.maximumDistanceSquared = maxDistance * maxDistance;
                                    sendInfoText(Text.literal(String.format(Text.translatable("playerfinder.finddistance.minandmax").getString(), minDistance, maxDistance)));
                                    return 1;
                                }))));
        registerToggle(dispatcher, "findnames", "playerfinder.findnames",
                toggleResult -> PlayerFinder.forceRealNames = getNewValueFromToggleResult(toggleResult, PlayerFinder.forceRealNames)
        );
        registerToggle(dispatcher, "findeyeheight", "playerfinder.findeyeheight",
                toggleResult -> Config.renderEyeHeight = getNewValueFromToggleResult(toggleResult, Config.renderEyeHeight)
        );
        registerToggle(dispatcher, "findfacing", "playerfinder.findfacing",
                toggleResult -> Config.renderFacing = getNewValueFromToggleResult(toggleResult, Config.renderFacing)
        );
    }

    private static void registerToggle(CommandDispatcher<FabricClientCommandSource> dispatcher, String commandName, String translationKey, Consumer<ToggleResult> onCommand) {
        dispatcher.register(ClientCommandManager.literal(commandName)
                .executes(context -> {
                    onCommand.accept(ToggleResult.TOGGLED);
                    sendInfoText(Text.translatable(translationKey + "." + (PlayerFinder.forceRealNames ? "enabled" : "disabled")));
                    return 1;
                })
                .then(ClientCommandManager.literal("enable").executes(
                        context -> {
                            onCommand.accept(ToggleResult.ENABLED);
                            sendInfoText(Text.translatable(translationKey + ".enabled"));
                            return 1;
                        }
                ))
                .then(ClientCommandManager.literal("disable").executes(
                        context -> {
                            onCommand.accept(ToggleResult.DISABLED);
                            sendInfoText(Text.translatable(translationKey + ".disabled"));
                            return 1;
                        }
                ))
        );
    }

    private static void sendInfoMessage(Text text) {
        if (MinecraftClient.getInstance().player == null) return;
        MutableText message = prefix.copy();
        message.append(text);
        MinecraftClient.getInstance().player.sendMessage(message);
    }

    private static void sendInfoText(Text text) {
        if (MinecraftClient.getInstance().player == null) return;
        MutableText message = prefix.copy();
        message.append(text).formatted(Formatting.GRAY);
        MinecraftClient.getInstance().player.sendMessage(message, true);
    }

    private static boolean getNewValueFromToggleResult(ToggleResult toggleResult, boolean oldValue) {
        return switch (toggleResult) {
            case ENABLED -> true;
            case DISABLED -> false;
            case TOGGLED -> !oldValue;
        };
    }

    private enum ToggleResult {
        ENABLED,
        DISABLED,
        TOGGLED
    }

    private static class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            if (MinecraftClient.getInstance().player == null) return builder.buildFuture();
            String remaining = builder.getRemaining();
            for (PlayerListEntry playerInfo : MinecraftClient.getInstance().player.networkHandler.getPlayerList()) {
                String playerName = playerInfo.getProfile().getName();
                if(playerName.toLowerCase().startsWith(remaining.toLowerCase())) builder.suggest(playerName);
            }
            return builder.buildFuture();
        }
    }
}
