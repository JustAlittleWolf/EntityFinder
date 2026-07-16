package me.wolfii.entityfinder.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.wolfii.entityfinder.EntityFinder;
import me.wolfii.entityfinder.EntityFinderSettings;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityRenderDispatcherMixin {
    @WrapMethod(method = "showHitboxes")
    private void checkCancelRenderDefaultHiboxes(Entity entity, float partialTicks, boolean isServerEntity, Operation<Void> original) {
        if (EntityFinder.shouldRender && EntityFinderSettings.hideVanillaHitboxes) return;
        original.call(entity, partialTicks, isServerEntity);
    }
}
