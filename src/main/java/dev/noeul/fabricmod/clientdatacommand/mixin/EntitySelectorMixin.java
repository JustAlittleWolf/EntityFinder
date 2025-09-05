package dev.noeul.fabricmod.clientdatacommand.mixin;

import dev.noeul.fabricmod.clientdatacommand.ClientEntitySelector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Noeul
 * <br>
 * License MIT
 * <br>
 * <a href="https://github.com/No-Eul/ClientDataCommand/blob/2268f9e9ee18b8e29987a7f819f46b4dd348fe75/src/main/java/dev/noeul/fabricmod/clientdatacommand/mixin/EntitySelectorMixin.java">Source</a>
 * <br>
 * Contains some modifications to allow it to work on modern versions
 */
@Environment(EnvType.CLIENT)
@Mixin(EntitySelector.class)
public class EntitySelectorMixin implements ClientEntitySelector {
    @Override
    public EntitySelector clientDataCommand$this() {
        return (EntitySelector) (Object) this;
    }
}
