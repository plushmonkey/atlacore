package com.plushnode.atlacore.events;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class CooldownRemoveEvent extends AbstractEvent {
    private User user;
    private AbilityDescription desc;
    private Cause cause;

    public CooldownRemoveEvent(User user, AbilityDescription abilityDescription, Cause cause) {
        this.user = user;
        this.desc = abilityDescription;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
