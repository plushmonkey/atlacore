package com.plushnode.atlacore.entity;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.EntityWrapper;
import com.plushnode.atlacore.platform.LivingEntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class EntityFactory {
    private EntityFactory() {

    }

    public static EntityWrapper createEntity(Entity entity) {
        if (entity instanceof Player) {
            com.plushnode.atlacore.platform.Player result = Game.getPlayerService().getPlayerByName(entity.getName());

            if (result == null) {
                return new BukkitBendingPlayer((Player) entity);
            }

            return (EntityWrapper)result;
        }

        if (entity instanceof LivingEntity)
            return new LivingEntityWrapper((LivingEntity)entity);

        return new EntityWrapper(entity);
    }
}
