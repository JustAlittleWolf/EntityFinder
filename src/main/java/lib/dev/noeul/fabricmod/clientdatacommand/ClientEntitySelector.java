package lib.dev.noeul.fabricmod.clientdatacommand;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Noeul
 * <br>
 * License MIT
 * <br>
 * <a href="https://github.com/No-Eul/ClientDataCommand/blob/2268f9e9ee18b8e29987a7f819f46b4dd348fe75/src/main/java/dev/noeul/fabricmod/clientdatacommand/ClientEntitySelector.java">Source</a>
 * <br>
 * Contains some modifications to allow it to work on modern versions
 */
public interface ClientEntitySelector {
    EntitySelector clientDataCommand$this();

    default List<? extends Entity> getEntities(FabricClientCommandSource source) throws CommandSyntaxException {
        if (!this.clientDataCommand$this().includesNonPlayers()) {
            return this.getPlayers(source);
        } else if (this.clientDataCommand$this().playerName != null) {
            PlayerEntity serverPlayerEntity = EntitySelectorHelper.getPlayer(source.getWorld(), this.clientDataCommand$this().playerName);
            return serverPlayerEntity == null ? List.of() : List.of(serverPlayerEntity);
        } else if (this.clientDataCommand$this().uuid != null) {
            ClientWorld serverWorld = source.getWorld();
            Entity entity = EntitySelectorHelper.getEntity(serverWorld, this.clientDataCommand$this().uuid);
            if (entity != null) {
                if (entity.getType().isEnabled(source.getEnabledFeatures())) {
                    return List.of(entity);
                }
            }

            return List.of();
        } else {
            Vec3d vec3d = this.clientDataCommand$this().positionOffset.apply(source.getPosition());
            Box box = this.clientDataCommand$this().getOffsetBox(vec3d);
            if (this.clientDataCommand$this().isSenderOnly()) {
                Predicate<Entity> predicate = this.clientDataCommand$this().getPositionPredicate(vec3d, box, null);
                return source.getEntity() != null && predicate.test(source.getEntity()) ? List.of(source.getEntity()) : List.of();
            } else {
                Predicate<Entity> predicate = this.clientDataCommand$this().getPositionPredicate(vec3d, box, source.getEnabledFeatures());
                List<Entity> list = new ObjectArrayList<>();
                this.appendEntitiesFromWorld(list, source.getWorld(), box, predicate);

                return this.clientDataCommand$this().getEntities(vec3d, list);
            }
        }
    }

    default List<PlayerEntity> getPlayers(FabricClientCommandSource source) {
        if (this.clientDataCommand$this().playerName != null) {
            PlayerEntity serverPlayerEntity = EntitySelectorHelper.getPlayer(source.getWorld(), this.clientDataCommand$this().playerName);
            return serverPlayerEntity == null ? List.of() : List.of(serverPlayerEntity);
        } else if (this.clientDataCommand$this().uuid != null) {
            PlayerEntity serverPlayerEntity = EntitySelectorHelper.getPlayer(source.getWorld(), this.clientDataCommand$this().uuid);
            return serverPlayerEntity == null ? List.of() : List.of(serverPlayerEntity);
        } else {
            Vec3d vec3d = this.clientDataCommand$this().positionOffset.apply(source.getPosition());
            Box box = this.clientDataCommand$this().getOffsetBox(vec3d);
            Predicate<Entity> predicate = this.clientDataCommand$this().getPositionPredicate(vec3d, box, null);
            if (this.clientDataCommand$this().isSenderOnly()) {
                if (source.getEntity() instanceof PlayerEntity serverPlayerEntity2 && predicate.test(serverPlayerEntity2)) {
                    return List.of(serverPlayerEntity2);
                }

                return List.of();
            } else {
                int i = this.clientDataCommand$this().getAppendLimit();
                List<PlayerEntity> list;
                if (this.clientDataCommand$this().isLocalWorldOnly()) {
                    list = EntitySelectorHelper.getPlayers(source.getWorld(), predicate, i);
                } else {
                    list = new ObjectArrayList<>();
                    for (PlayerEntity serverPlayerEntity3 : source.getWorld().getPlayers()) {
                        if (predicate.test(serverPlayerEntity3)) {
                            list.add(serverPlayerEntity3);
                            if (list.size() >= i) {
                                return list;
                            }
                        }
                    }
                }

                return this.clientDataCommand$this().getEntities(vec3d, list);
            }
        }
    }

    default void appendEntitiesFromWorld(List<Entity> entities, World world, @Nullable Box box, Predicate<Entity> predicate) {
        int i = this.clientDataCommand$this().getAppendLimit();
        if (entities.size() < i) {
            if (box != null) {
                world.collectEntitiesByType(this.clientDataCommand$this().entityFilter, box, predicate, entities, i);
            } else {
                EntitySelectorHelper.collectEntitiesByType(world, this.clientDataCommand$this().entityFilter, predicate, entities, i);
            }

        }
    }
}
