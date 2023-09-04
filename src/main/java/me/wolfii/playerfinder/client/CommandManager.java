package me.wolfii.playerfinder.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.render.Rendermode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class CommandManager {
    private static final MutableText prefix = Text.literal("[PlayerFinder] ").formatted(Formatting.GRAY);

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        assert MinecraftClient.getInstance().player != null;
        dispatcher.register(ClientCommandManager.literal("find")
                .then(ClientCommandManager.argument("playername", greedyString())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            PlayerFinder.hightLightAll = false;
                            if (PlayerFinder.rendermode == Rendermode.NONE) {
                                PlayerFinder.rendermode = PlayerFinder.lastRendermode;
                                MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderHitboxes(true);
                            }
                            String playerName = context.getArgument("playername", String.class);
                            if (PlayerFinder.highlightedPlayers.stream().noneMatch(username -> username.equalsIgnoreCase(playerName))) {
                                PlayerFinder.highlightedPlayers.add(playerName);
                            }
                            MutableText message = prefix.copy();
                            message.append(Text.literal(String.format(Text.translatable("playerfinder.find.specific").getString(), playerName)).formatted(Formatting.GRAY));
                            MinecraftClient.getInstance().player.sendMessage(message, true);
                            return 1;
                        })));
        dispatcher.register(ClientCommandManager.literal("findall")
                .executes(context -> {
                    PlayerFinder.hightLightAll = true;
                    PlayerFinder.highlightedPlayers.clear();
                    MutableText message = prefix.copy();
                    message.append(Text.translatable("playerfinder.find.all").formatted(Formatting.GRAY));
                    MinecraftClient.getInstance().player.sendMessage(message, true);
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("findnone")
                .executes(context -> {
                    PlayerFinder.hightLightAll = false;
                    PlayerFinder.highlightedPlayers.clear();
                    MutableText message = prefix.copy();
                    message.append(Text.translatable("playerfinder.find.none").formatted(Formatting.GRAY));
                    MinecraftClient.getInstance().player.sendMessage(message, true);
                    return 1;
                }));
        dispatcher.register(ClientCommandManager.literal("findlist")
                .executes(context -> {
                    MutableText message = prefix.copy();
                    message.append(Text.translatable("playerfinder.find.highlighting").formatted(Formatting.WHITE));
                    if (PlayerFinder.hightLightAll) {
                        message.append(Text.translatable("playerfinder.find.everyone").formatted(Formatting.BOLD).formatted(Formatting.WHITE));
                    } else if (PlayerFinder.highlightedPlayers.isEmpty()) {
                        message.append(Text.translatable("playerfinder.find.nobody").formatted(Formatting.BOLD).formatted(Formatting.WHITE));
                    } else {
                        message.append(Text.literal(String.join(", ", PlayerFinder.highlightedPlayers)).formatted(Formatting.WHITE));
                    }
                    MinecraftClient.getInstance().player.sendMessage(message);
                    return 1;
                }));
    }

    private static class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            if (MinecraftClient.getInstance().world == null) return builder.buildFuture();
            for (AbstractClientPlayerEntity suggestion : MinecraftClient.getInstance().world.getPlayers()) {
                builder.suggest(suggestion.getGameProfile().getName());
            }
            return builder.buildFuture();}
    }
}
