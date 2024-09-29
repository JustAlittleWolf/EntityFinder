package me.wolfii.playerfinder.client;

import me.wolfii.playerfinder.PlayerFinder;
import net.minecraft.client.MinecraftClient;

public class HitboxChecker {
    public static void afterTick(MinecraftClient minecraftClient) {
        if (!PlayerFinder.renderingActive) return;
        if (minecraftClient.getEntityRenderDispatcher().shouldRenderHitboxes()) return;
        PlayerFinder.renderingActive = false;
    }
}
