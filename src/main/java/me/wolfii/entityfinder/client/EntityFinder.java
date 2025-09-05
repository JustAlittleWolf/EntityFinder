package me.wolfii.entityfinder.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import dev.noeul.fabricmod.clientdatacommand.ClientEntitySelector;
import me.wolfii.entityfinder.EntityFinderSettings;
import me.wolfii.entityfinder.command.EntityFinderCommandManager;
import me.wolfii.entityfinder.render.EntityFinderRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityFinder implements ClientModInitializer {
    public static final List<ClientEntitySelector> highlighted = new ArrayList<>();
    public static final List<ClientEntitySelector> hidden = new ArrayList<>();
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Entity> highlightedEntities = new HashSet<>();
    public static boolean shouldRender = false;

    private static void checkForDisableRendering(MinecraftClient minecraftClient) {
        if (!shouldRender) return;
        if (minecraftClient.getEntityRenderDispatcher().shouldRenderHitboxes()) return;
        shouldRender = false;
        hidden.clear();
        highlighted.clear();
        highlightedEntities.clear();
    }

    @SuppressWarnings("DataFlowIssue")
    private static void updateHighlightedEntities(MinecraftClient minecraftClient) {
        highlightedEntities.clear();
        if (!shouldRender) return;

        FabricClientCommandSource source = (FabricClientCommandSource) new ClientCommandSource(minecraftClient.getNetworkHandler(), minecraftClient);
        if (minecraftClient.player == null) return;

        try {
            for (ClientEntitySelector highlightedSelector : highlighted) {
                highlightedEntities.addAll(highlightedSelector.getEntities(source));
            }
            for (ClientEntitySelector hiddenSelector : hidden) {
                highlightedEntities.removeAll(hiddenSelector.getEntities(source));
            }
        } catch (CommandSyntaxException e) {
            LOGGER.error("Encountered issue while getting entities", e);
        }
        highlightedEntities.removeIf(entity -> {
            double distanceSquared = entity.squaredDistanceTo(source.getPlayer());
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

        WorldRenderEvents.LAST.register(EntityFinderRenderer::render);

        ClientTickEvents.START_CLIENT_TICK.register(EntityFinder::updateHighlightedEntities);
        ClientTickEvents.END_CLIENT_TICK.register(EntityFinder::checkForDisableRendering);
    }
}

