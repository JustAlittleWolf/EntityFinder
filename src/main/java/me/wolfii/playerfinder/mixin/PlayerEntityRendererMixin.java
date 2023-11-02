package me.wolfii.playerfinder.mixin;

import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.render.EntityHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @ModifyArgs(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 1))
    public void modifyText(Args args) {
        AbstractClientPlayerEntity abstractClientPlayerEntity = args.get(0);
        if (!PlayerFinder.forceRealNames) {
            if (!Config.showRealNamesWhenHighlighted) return;
            if (!EntityHelper.shouldHighlightEntityCached(abstractClientPlayerEntity)) return;
        }
        Text modifiedName = Text
                .literal(abstractClientPlayerEntity.getGameProfile().getName())
                .setStyle(abstractClientPlayerEntity.getDisplayName().getStyle());
        args.set(1, modifiedName);
    }
}
