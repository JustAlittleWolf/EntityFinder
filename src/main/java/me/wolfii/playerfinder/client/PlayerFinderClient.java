package me.wolfii.playerfinder.client;

import me.wolfii.playerfinder.render.PlayerfinderRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class PlayerFinderClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.LAST.register(PlayerfinderRenderer::render);

        ClientCommandRegistrationCallback.EVENT.register(CommandManager::registerCommand);
    }
}
