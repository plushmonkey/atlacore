package com.plushnode.atlacore.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PlayerToggleSneakEvent extends AbstractEvent {
    private final Player player;
    private final Cause cause;
    private final boolean isSneaking;

    public PlayerToggleSneakEvent(Player player, boolean sneaking, Cause cause) {
        this.player = player;
        this.isSneaking = sneaking;
        this.cause = cause;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
