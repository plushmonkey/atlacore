package com.plushnode.atlacore.events;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CooldownRemoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private User user;
    private AbilityDescription desc;

    public CooldownRemoveEvent(User user, AbilityDescription abilityDescription) {
        this.user = user;
        this.desc = abilityDescription;
    }

    public User getUser() {
        return user;
    }

    public AbilityDescription getAbilityDescription() {
        return desc;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
