package me.wolfii.entityfinder.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.wolfii.entityfinder.client.EntityFinder;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @WrapOperation(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSneaky()Z"))
    private boolean increaseSneakingRenderDistance(LivingEntity livingEntity, Operation<Boolean> original) {
        if (EntityFinder.shouldHighlight(livingEntity)) return false;
        return original.call(livingEntity);
    }

    @WrapOperation(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean renderLabelWhenInvisible(LivingEntity livingEntity, PlayerEntity playerEntity, Operation<Boolean> original) {
        if (EntityFinder.shouldHighlight(livingEntity)) return false;
        return original.call(livingEntity, playerEntity);
    }
}
