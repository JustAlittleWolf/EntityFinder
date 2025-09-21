package lib.dev.noeul.fabricmod.clientdatacommand.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lib.dev.noeul.fabricmod.clientdatacommand.ClientEntityArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author JustAlittleWolf
 * <br>
 * License MIT
 * <br>
 * Allows at selectors to be used client side even when restricted by the server
 */
@Mixin(EntityArgumentType.class)
public class EntityArgumentTypeMixin implements ClientEntityArgumentType {
    @Unique
    public boolean alwaysAllowAtSelectors;

    @Unique
    public EntityArgumentType clientDataCommand$withAlwaysAllowAtSelectors() {
        alwaysAllowAtSelectors = true;
        return (EntityArgumentType) (Object) this;
    }

    @WrapOperation(
            method = "parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lnet/minecraft/command/EntitySelector;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/EntitySelectorReader;shouldAllowAtSelectors(Ljava/lang/Object;)Z"
            )
    )
    private <S> boolean parseOverrideShouldAllowAtSelectors(S source, Operation<Boolean> original) {
        if (alwaysAllowAtSelectors) return true;
        return original.call(source);
    }

    @WrapOperation(
            method = "listSuggestions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/EntitySelectorReader;shouldAllowAtSelectors(Ljava/lang/Object;)Z"
            )
    )
    private <S> boolean listSuggestionsOverrideShouldAllowAtSelectors(S source, Operation<Boolean> original) {
        if (alwaysAllowAtSelectors) return true;
        return original.call(source);
    }
}
