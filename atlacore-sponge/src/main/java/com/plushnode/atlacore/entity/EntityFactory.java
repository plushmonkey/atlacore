package com.plushnode.atlacore.entity;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.SpongeBendingPlayer;
import com.plushnode.atlacore.platform.EntityWrapper;
import com.plushnode.atlacore.platform.LivingEntityWrapper;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;

public final class EntityFactory {
    private EntityFactory() {

    }

    public static EntityWrapper createEntity(Entity entity) {
        if (entity instanceof Player) {
            com.plushnode.atlacore.platform.Player result = Game.getPlayerService().getPlayerByName(((Player) entity).getName());

            if (result == null) {
                return new SpongeBendingPlayer((Player) entity);
            }

            return (EntityWrapper)result;
        }

        if (entity instanceof Living)
            return new LivingEntityWrapper(entity);

        return new EntityWrapper(entity);
    }
}
