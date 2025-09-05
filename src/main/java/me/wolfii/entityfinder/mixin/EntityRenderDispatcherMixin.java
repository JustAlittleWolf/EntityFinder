package me.wolfii.entityfinder.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.wolfii.entityfinder.EntityFinderSettings;
import me.wolfii.entityfinder.client.EntityFinder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityHitbox;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @WrapMethod(method = "renderHitbox")
    private static void checkCancelRenderDefaultHiboxes(MatrixStack matrices, VertexConsumer vertexConsumer, EntityHitbox hitbox, Operation<Void> original) {
        if (EntityFinder.shouldRender && EntityFinderSettings.hideVanillaHitboxes) return;
        original.call(matrices, vertexConsumer, hitbox);
    }
}
