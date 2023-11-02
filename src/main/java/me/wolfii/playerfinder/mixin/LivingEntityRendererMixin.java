package me.wolfii.playerfinder.mixin;

import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.render.EntityHelper;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Redirect(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSneaky()Z"))
    private boolean increaseSneakingRenderDistance(LivingEntity instance) {
        return !EntityHelper.shouldHighlightEntityCached(instance, !instance.isSneaking());
    }

    @Redirect(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean renderLabelWhenInvisible(LivingEntity instance, PlayerEntity playerEntity) {
        return !EntityHelper.shouldHighlightEntityCached(instance, !instance.isInvisibleTo(playerEntity));
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void forceRenderLabelWhenForceRealNames(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if(!(livingEntity instanceof PlayerEntity)) return;
        if(EntityHelper.getOwnUUID() == livingEntity.getUuid()) return;
        if (PlayerFinder.forceRealNames) cir.setReturnValue(true);
    }
}
