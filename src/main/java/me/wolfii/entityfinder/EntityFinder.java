package me.wolfii.entityfinder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import me.wolfii.clientdatacommandselector.ClientEntitySelector;
import me.wolfii.clientdatacommandselector.FabricClientCommandSourceStack;
import me.wolfii.entityfinder.command.EntityFinderCommandManager;
import me.wolfii.entityfinder.render.EntityFinderRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.util.*;

public class EntityFinder implements ClientModInitializer {
    public static final List<ClientEntitySelector> highlighted = new ArrayList<>();
    public static final List<ClientEntitySelector> hidden = new ArrayList<>();
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Entity> highlightedEntities = new HashSet<>();
    public static boolean shouldRender = false;

    private static void checkForDisableRendering(Minecraft minecraftClient) {
        if (!shouldRender) return;
        if (minecraftClient.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) return;
        shouldRender = false;
        hidden.clear();
        highlighted.clear();
        highlightedEntities.clear();
    }

    private static void updateHighlightedEntities(Minecraft minecraftClient) {
        highlightedEntities.clear();
        if (!shouldRender) return;
        if (minecraftClient.player == null) return;
        FabricClientCommandSourceStack source = FabricClientCommandSourceStack.fromMinecraft(minecraftClient);
        try {
            for (ClientEntitySelector highlightedSelector : highlighted) {
                highlightedEntities.addAll(highlightedSelector.findEntities(source));
            }
            for (ClientEntitySelector hiddenSelector : hidden) {
                hiddenSelector.findEntities(source).forEach(highlightedEntities::remove);
            }
        } catch (CommandSyntaxException e) {
            LOGGER.error("Encountered issue while getting entities", e);
        }
        highlightedEntities.removeIf(entity -> {
            double distanceSquared = entity.distanceToSqr(minecraftClient.player);
            return distanceSquared < EntityFinderSettings.minimumDistanceSquared || distanceSquared > EntityFinderSettings.maximumDistanceSquared;
        });
        if (EntityFinderSettings.hideSelf) highlightedEntities.remove(source.getPlayer());
    }

    public static Set<Entity> getHighlightedEntities() {
        return Collections.unmodifiableSet(highlightedEntities);
    }

    public static boolean shouldHighlight(Entity entity) {
        return shouldRender && highlightedEntities.contains(entity);
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(EntityFinderCommandManager::registerCommands);

        LevelRenderEvents.END_MAIN.register(EntityFinderRenderer::render);

        ClientTickEvents.START_CLIENT_TICK.register(EntityFinder::updateHighlightedEntities);
        ClientTickEvents.END_CLIENT_TICK.register(EntityFinder::checkForDisableRendering);
    }
}

