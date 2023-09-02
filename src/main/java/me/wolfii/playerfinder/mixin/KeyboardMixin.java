package me.wolfii.playerfinder.mixin;

import me.wolfii.playerfinder.PlayerFinder;
import me.wolfii.playerfinder.render.Rendermode;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)

public abstract class KeyboardMixin {
    @Unique
    private static final MutableText prefix = Text.literal("[PlayerFinder] ").formatted(Formatting.GRAY);

    @Inject(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;setRenderHitboxes(Z)V", shift = At.Shift.AFTER))
    private void hitboxEnablingCheck(int key, CallbackInfoReturnable<Boolean> cir) {
        boolean renderHitboxes = MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes();
        if(!renderHitboxes) {
            if(PlayerFinder.rendermode == Rendermode.NONE) return;
            sendFeedback("playerfinder.feedback.disabled");
            PlayerFinder.rendermode = Rendermode.NONE;
            return;
        }
        boolean shift = Screen.hasShiftDown();
        boolean alt = Screen.hasAltDown();
        if (shift && alt) {
            PlayerFinder.rendermode = Rendermode.BOTH;
            sendFeedback("playerfinder.feedback.both");
        } else if (shift) {
            PlayerFinder.rendermode = Rendermode.HITBOXES;
            sendFeedback("playerfinder.feedback.hitboxes");
        } else if (alt) {
            PlayerFinder.rendermode = Rendermode.TRACERS;
            sendFeedback("playerfinder.feedback.tracers");
        }
    }

    @Unique
    private void sendFeedback(String translatable) {
        if(MinecraftClient.getInstance().player == null) return;
        MutableText message = prefix.copy();
        message.append(Text.translatable(translatable).formatted(Formatting.GRAY));
        MinecraftClient.getInstance().player.sendMessage(message, true);
    }
}
