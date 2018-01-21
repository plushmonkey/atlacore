package com.plushnode.atlacore.entity;

import com.plushnode.atlacore.BendingPlayer;
import com.plushnode.atlacore.wrappers.EntityWrapper;
import com.plushnode.atlacore.wrappers.LivingEntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class EntityFactory {
    private EntityFactory() {

    }

    public static EntityWrapper createEntity(Entity entity) {
        if (entity instanceof Player)
            return new BendingPlayer((Player)entity);
        if (entity instanceof LivingEntity)
            return new LivingEntityWrapper((LivingEntity)entity);
        return new EntityWrapper(entity);
    }
}
