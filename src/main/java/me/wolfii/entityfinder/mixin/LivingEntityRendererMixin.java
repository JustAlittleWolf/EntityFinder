package me.wolfii.entityfinder.mixin;

import me.wolfii.entityfinder.client.EntityFinder;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Redirect(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSneaky()Z"))
    private boolean increaseSneakingRenderDistance(T livingEntity) {
        if (EntityFinder.shouldHighlight(livingEntity)) return false;
        return livingEntity.isSneaking();
    }

    @Redirect(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean renderLabelWhenInvisible(T livingEntity, PlayerEntity playerEntity) {
        if (EntityFinder.shouldHighlight(livingEntity)) return false;
        return livingEntity.isInvisibleTo(playerEntity);
    }
}
