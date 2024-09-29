package me.wolfii.playerfinder.render;

import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.PlayerFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class EntityHelper {
    private static final Set<Entity> highlightedEntities = new HashSet<>();

    public static boolean shouldHighlightEntityCached(Entity entity) {
        return highlightedEntities.contains(entity);
    }

    public static void updateHighlightedEntities() {
        highlightedEntities.clear();
        if(!PlayerFinder.renderingActive) return;

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.world == null) return;

        for (Entity entity : minecraftClient.world.getPlayers()) {
            if (EntityHelper.shouldHighlightEntity(entity)) {
                highlightedEntities.add(entity);
            }
        }
    }

    private static boolean shouldHighlightEntity(Entity entity) {
        if (entity.getType() != EntityType.PLAYER) return false;

        MinecraftClient minecraft = MinecraftClient.getInstance();

        if (minecraft.player != null) {
            if (minecraft.player.equals(entity)) return false;
            Vec3d entityPos = entity.getPos();
            Vec3d comparingPos = minecraft.player.getPos();
            if (minecraft.options.getPerspective().isFirstPerson() && minecraft.cameraEntity != null) {
                comparingPos = minecraft.cameraEntity.getPos();
            }
            double squaredDistance = comparingPos.squaredDistanceTo(entityPos);
            if (squaredDistance < Config.minimumDistanceSquared) return false;
            if (squaredDistance > Config.maximumDistanceSquared) return false;
        }

        if (PlayerFinder.hightLightAll) return true;
        return PlayerFinder.highlightedNames.stream().anyMatch(name -> name.equalsIgnoreCase(((PlayerEntity) entity).getGameProfile().getName()));
    }

    public static Set<Entity> getEntitiesToHighlight() {
        return highlightedEntities;
    }

    public static Box getOffsetBoundingBox(Entity entity, float tickDelta) {
        double offsetX = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
        double offsetY = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
        double offsetZ = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();
        return entity.getBoundingBox().offset(offsetX, offsetY, offsetZ);
    }
}
