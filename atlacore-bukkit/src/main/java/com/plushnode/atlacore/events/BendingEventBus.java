package com.plushnode.atlacore.events;

import com.plushnode.atlacore.event.EventBus;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import org.bukkit.Bukkit;

public class BendingEventBus implements EventBus {
    @Override
    public void postBendingPlayerCreateEvent(Player player) {
        Bukkit.getPluginManager().callEvent(new BendingPlayerCreateEvent(player));
    }

    @Override
    public void postCooldownAddEvent(User user, AbilityDescription ability) {
        Bukkit.getPluginManager().callEvent(new CooldownAddEvent(user, ability));
    }

    @Override
    public void postCooldownRemoveEvent(User user, AbilityDescription ability) {
        Bukkit.getPluginManager().callEvent(new CooldownRemoveEvent(user, ability));
    }
}
