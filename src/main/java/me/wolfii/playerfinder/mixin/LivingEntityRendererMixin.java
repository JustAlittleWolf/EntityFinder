package me.wolfii.playerfinder.mixin;

import me.wolfii.playerfinder.render.EntityHelper;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Redirect(method="hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at=@At(value="INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSneaky()Z"))
    private boolean increaseSneakingRenderDistance(LivingEntity instance) {
        return !EntityHelper.shouldHighlightEntityCached(instance, !instance.isSneaking());
    }
    @Redirect(method="hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at=@At(value="INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean renderLabelWhenInvisible(LivingEntity instance, PlayerEntity playerEntity) {
        return !EntityHelper.shouldHighlightEntityCached(instance, !instance.isInvisibleTo(playerEntity));
    }
}
