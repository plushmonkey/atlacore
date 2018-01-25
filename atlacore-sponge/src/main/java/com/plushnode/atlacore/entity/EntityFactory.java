package com.plushnode.atlacore.entity;

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
        if (entity instanceof Player)
            return new SpongeBendingPlayer((Player)entity);
        if (entity instanceof Living)
            return new LivingEntityWrapper((Living)entity);
        return new EntityWrapper(entity);
    }
}
