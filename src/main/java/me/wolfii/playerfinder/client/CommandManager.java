package me.wolfii.playerfinder.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.render.EntityHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.StringArgumentType;

public class CommandManager {
    private static final MutableText prefix = Text.literal("[PlayerFinder] ").formatted(Formatting.GRAY);

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess ignoredRegistryAccess) {
        dispatcher.register(ClientCommandManager.literal("find")
                .then(ClientCommandManager.argument("playername", StringArgumentType.greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            PlayerFinder.hightLightAll = false;
                            if (!PlayerFinder.renderingActive) {
                                PlayerFinder.renderingActive = true;
                                MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(true);
                                DebugMessage.debugLog("debug.show_hitboxes.on");
                            }
                            String playerName = context.getArgument("playername", String.class);
                            PlayerFinder.highlightedNames.add(playerName.toLowerCase());
                            EntityHelper.updateHighlightedEntities();
                            sendInfoOverlay(Text.literal("Highlighting " + playerName));
                            return 1;
                        })));
        dispatcher.register(ClientCommandManager.literal("findall")
                .executes(context -> {
                    PlayerFinder.hightLightAll = true;
                    if (!PlayerFinder.renderingActive) {
                        PlayerFinder.renderingActive = true;
                        MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(true);
                        DebugMessage.debugLog("debug.show_hitboxes.on");
                    }
                    PlayerFinder.highlightedNames.clear();
                    EntityHelper.updateHighlightedEntities();
                    sendInfoOverlay(Text.literal("Highlighting all players"));
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("findlist")
                .executes(context -> {
                    MutableText message = Text.literal("Currently highlighting: ").formatted(Formatting.WHITE);
                    if (PlayerFinder.hightLightAll) {
                        message.append(Text.literal("Everyone").formatted(Formatting.BOLD, Formatting.WHITE));
                    } else if (PlayerFinder.highlightedNames.isEmpty()) {
                        message.append(Text.literal("Nobody").formatted(Formatting.BOLD, Formatting.WHITE));
                    } else {
                        message.append(Text.literal(String.join(", ", PlayerFinder.highlightedNames)).formatted(Formatting.WHITE));
                    }
                    sendInfoMessage(message);
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("unfind")
                .then(ClientCommandManager.argument("playername", StringArgumentType.greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            String playerName = context.getArgument("playername", String.class);
                            PlayerFinder.highlightedNames.removeIf(playerName::equalsIgnoreCase);
                            EntityHelper.updateHighlightedEntities();
                            sendInfoOverlay(Text.literal("No longer highlighting " + playerName));
                            return 1;
                        })));
        dispatcher.register(ClientCommandManager.literal("finddistance")
                .then(ClientCommandManager.argument("minimum", DoubleArgumentType.doubleArg(0.0, 1024.0))
                        .executes(context -> {
                            double minDistance = context.getArgument("minimum", Double.class);
                            Config.minimumDistanceSquared = Math.max(Math.min(minDistance * minDistance, 128 * 128), 0.0);
                            sendInfoOverlay(Text.literal("Set minimum distance to " + minDistance));
                            return 1;
                        })
                        .then(ClientCommandManager.argument("maximum", DoubleArgumentType.doubleArg(0.0, 4096.0))
                                .executes(context -> {
                                    double minDistance = context.getArgument("minimum", Double.class);
                                    Config.minimumDistanceSquared = minDistance * minDistance;
                                    double maxDistance = context.getArgument("maximum", Double.class);
                                    Config.maximumDistanceSquared = maxDistance * maxDistance;
                                    sendInfoOverlay(Text.literal("Set minimum distance to " + minDistance + " and maximum distance to " + maxDistance));
                                    return 1;
                                }))));

        // BROKEN FOR NOW
        /*dispatcher.register(ClientCommandManager.literal("findnames")
                .executes(context -> {
                    PlayerFinder.forceRealNames = !PlayerFinder.forceRealNames;
                    sendInfoOverlay(Text.literal((PlayerFinder.forceRealNames ? "Enabled" : "Disabled") + " real names"));
                    return 1;
                }));*/
    }

    private static void sendInfoMessage(Text text) {
        if (MinecraftClient.getInstance().player == null) return;
        MutableText message = prefix.copy();
        message.append(text);
        MinecraftClient.getInstance().player.sendMessage(message);
    }

    private static void sendInfoOverlay(Text text) {
        if (MinecraftClient.getInstance().player == null) return;
        MutableText message = prefix.copy();
        message.append(text).formatted(Formatting.GRAY);
        MinecraftClient.getInstance().player.sendMessage(message, true);
    }

    private static class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            if (MinecraftClient.getInstance().player == null) return builder.buildFuture();
            String remaining = builder.getRemaining();
            for (PlayerListEntry playerInfo : MinecraftClient.getInstance().player.networkHandler.getPlayerList()) {
                String playerName = playerInfo.getProfile().getName();
                if (playerName.toLowerCase().startsWith(remaining.toLowerCase())) builder.suggest(playerName);
            }
            return builder.buildFuture();
        }
    }
}
