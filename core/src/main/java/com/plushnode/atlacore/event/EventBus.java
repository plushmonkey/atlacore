package com.plushnode.atlacore.event;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.User;

public interface EventBus {
    void postCooldownAddEvent(User user, AbilityDescription ability);
    void postCooldownRemoveEvent(User user, AbilityDescription ability);
}
