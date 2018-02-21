package com.plushnode.atlacore.events;

import com.plushnode.atlacore.platform.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// This occurs when the Player is created in memory, not in the database.
// This is posted even when the player has already joined the server before.
public class BendingPlayerCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;

    public BendingPlayerCreateEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
