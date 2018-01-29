package com.plushnode.atlacore.events;

import com.plushnode.atlacore.event.EventBus;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;

public class BendingEventBus implements EventBus {
    @Override
    public void postCooldownAddEvent(User user, AbilityDescription ability) {
        CooldownAddEvent event = new CooldownAddEvent(user, ability, Cause.source(this).build());
        Sponge.getEventManager().post(event);
    }

    @Override
    public void postCooldownRemoveEvent(User user, AbilityDescription ability) {
        CooldownRemoveEvent event = new CooldownRemoveEvent(user, ability, Cause.source(this).build());
        Sponge.getEventManager().post(event);
    }
}
