package lib.dev.noeul.fabricmod.clientdatacommand;

import net.minecraft.command.argument.EntityArgumentType;

/**
 * @author JustAlittleWolf
 * <br>
 * License MIT
 * <br>
 * Allows at selectors to be used client side even when restricted by the server
 */
public interface ClientEntityArgumentType {
    EntityArgumentType clientDataCommand$withAlwaysAllowAtSelectors();
}
