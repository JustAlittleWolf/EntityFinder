package me.wolfii.entityfinder.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EntityFinderFeedback {
    public static void sendDebugMessage(String translation_key) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                Text.empty()
                        .append(Text.translatable("debug.prefix")
                                .formatted(Formatting.YELLOW, Formatting.BOLD))
                        .append(ScreenTexts.SPACE)
                        .append(Text.translatable(translation_key))
        );
    }

    public static void sendInfoMessage(Text message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
    }
}
