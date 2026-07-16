package me.wolfii.clientdatacommandselector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ClientEntitySelector {
    Player clientdatacommandupdated$findSinglePlayerClient(FabricClientCommandSourceStack sender) throws CommandSyntaxException;

    List<? extends Player> clientdatacommandupdated$findPlayersClient(FabricClientCommandSourceStack sender) throws CommandSyntaxException;
}