package me.wolfii.playerfinder.render;

import me.wolfii.playerfinder.PlayerFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class EntityHelper {
    private static UUID ownUUID = null;
    public static boolean shouldHighlightEntity(Entity entity, boolean defaultValue) {
        if(ownUUID == null) ownUUID = MinecraftClient.getInstance().player == null ? null : MinecraftClient.getInstance().player.getUuid();
        if (entity instanceof PlayerEntity playerEntity && PlayerFinder.highlightNametags) {
            if(playerEntity.getUuid().equals(ownUUID)) return defaultValue;
            if(PlayerFinder.rendermode == Rendermode.NONE) return defaultValue;
            if(PlayerFinder.hightLightAll) return true;
            if(PlayerFinder.highlightedPlayers.stream().anyMatch(username -> username.equalsIgnoreCase(playerEntity.getGameProfile().getName()))) return true;
        }
        return defaultValue;
    }

    public static boolean shouldHighlightEntity(Entity entity) {
        return shouldHighlightEntity(entity, false);
    }
}
