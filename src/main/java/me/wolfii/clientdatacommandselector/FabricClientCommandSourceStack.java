package me.wolfii.clientdatacommandselector;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.NonNull;

public class FabricClientCommandSourceStack extends CommandSourceStack {

    private final FabricClientCommandSource clientSource;

    public FabricClientCommandSourceStack(FabricClientCommandSource clientSource) {
        super(
            null,
            clientSource.getPosition(),
            clientSource.getRotation(),
            null,
            clientSource.permissions(),
            clientSource.getEntity().getPlainTextName(),
            clientSource.getEntity().getDisplayName(),
            null,
            clientSource.getEntity()
        );
        this.clientSource = clientSource;
    }

    public FabricClientCommandSource getClientSource() {
        return clientSource;
    }

    @Override
    public @NonNull FeatureFlagSet enabledFeatures() {
        return clientSource.getLevel().enabledFeatures();
    }
}
