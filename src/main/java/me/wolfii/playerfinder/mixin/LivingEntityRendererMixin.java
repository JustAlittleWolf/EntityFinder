package me.wolfii.playerfinder.mixin;

import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.render.EntityHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.EntityType;
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
    private boolean increaseSneakingRenderDistance(T livingEntity) {
        if(EntityHelper.shouldHighlightEntityCached(livingEntity)) return false;
        return livingEntity.isSneaking();
    }

    @Redirect(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean renderLabelWhenInvisible(T livingEntity, PlayerEntity playerEntity) {
        if(EntityHelper.shouldHighlightEntityCached(livingEntity)) return false;
        return livingEntity.isInvisibleTo(playerEntity);
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void forceRenderLabelWhenForceRealNames(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!PlayerFinder.forceRealNames) return;
        if (livingEntity.getType() != EntityType.PLAYER) return;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.player != null && minecraftClient.player.equals(livingEntity)) return;
        cir.setReturnValue(true);
    }
}
