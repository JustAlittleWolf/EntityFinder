package me.wolfii.playerfinder.mixin;

import me.wolfii.playerfinder.render.EntityHelper;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Redirect(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaky()Z"))
    private boolean renderLabelWhenSneaky(Entity instance) {
        return !EntityHelper.shouldHighlightEntityCached(instance, !instance.isSneaking());
    }
}
