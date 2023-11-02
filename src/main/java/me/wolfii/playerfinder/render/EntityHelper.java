package me.wolfii.playerfinder.render;

import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.PlayerFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class EntityHelper {
    private static final HashSet<UUID> highlightedUUIDs = new HashSet<>();
    private static final ArrayList<PlayerEntity> highlightedPlayerEntities = new ArrayList<>();
    private static UUID ownUUID;

    public static boolean shouldHighlightEntityCached(Entity entity, boolean defaultValue) {
        return defaultValue || highlightedUUIDs.contains(entity.getUuid());
    }

    public static boolean shouldHighlightEntityCached(Entity entity) {
        return shouldHighlightEntityCached(entity, false);
    }

    public static UUID getOwnUUID() {
        return ownUUID;
    }

    public static void beforeTick(MinecraftClient minecraftClient) {
        highlightedUUIDs.clear();
        highlightedPlayerEntities.clear();

        if(minecraftClient.player != null) {
            ownUUID = minecraftClient.player.getUuid();
        }

        if(minecraftClient.world == null) return;

        for (Entity entity : minecraftClient.world.getEntities()) {
            if(EntityHelper.shouldHighlightEntity(entity)) {
                highlightedUUIDs.add(entity.getUuid());
                highlightedPlayerEntities.add((PlayerEntity) entity);
            }
        }
    }

    private static boolean shouldHighlightEntity(Entity entity) {
        if (PlayerFinder.rendermode == Rendermode.NONE) return false;

        //@Todo compare to camera when in first person

        if (MinecraftClient.getInstance().player != null) {
            double squaredDistance = MinecraftClient.getInstance().player.getPos().squaredDistanceTo(entity.getPos());
            if (squaredDistance < Config.minimumDistanceSquared) return false;
            if(squaredDistance > Config.maximumDistanceSquared) return false;
        }

        if (entity instanceof PlayerEntity playerEntity && Config.renderNametagsThroughWalls) {
            if (PlayerFinder.hightLightAll) return true;
            return PlayerFinder.highlightedPlayers.stream().anyMatch(username -> username.equalsIgnoreCase(playerEntity.getGameProfile().getName()));
        }
        return false;
    }

    public static ArrayList<PlayerEntity> getPlayerEntitiesToHighlight() {
        return highlightedPlayerEntities;
    }

    public static Box getOffsetBoundingBox(PlayerEntity playerEntity, float tickDelta) {
        double offsetX = MathHelper.lerp(tickDelta, playerEntity.lastRenderX, playerEntity.getX()) - playerEntity.getX();
        double offsetY = MathHelper.lerp(tickDelta, playerEntity.lastRenderY, playerEntity.getY()) - playerEntity.getY();
        double offsetZ = MathHelper.lerp(tickDelta, playerEntity.lastRenderZ, playerEntity.getZ()) - playerEntity.getZ();
        return playerEntity.getBoundingBox().offset(offsetX, offsetY, offsetZ);
    }
}
