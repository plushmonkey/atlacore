package com.plushnode.atlacore.events;

import com.plushnode.atlacore.platform.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

// This occurs when the Player is created in memory, not in the database.
// This is posted even when the player has already joined the server before.
public class BendingPlayerCreateEvent extends AbstractEvent {
    private Player player;

    public BendingPlayerCreateEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public Cause getCause() {
        return null;
    }
}

