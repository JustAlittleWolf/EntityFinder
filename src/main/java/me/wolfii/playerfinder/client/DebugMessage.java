package me.wolfii.playerfinder.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DebugMessage {

    public static void debugLog(String text) {
        addDebugMessage(Formatting.YELLOW, Text.translatable(text));
    }

    private static void addDebugMessage(Formatting formatting, Text text) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(Text.translatable("debug.prefix").formatted(formatting, Formatting.BOLD)).append(ScreenTexts.SPACE).append(text));
    }
}
