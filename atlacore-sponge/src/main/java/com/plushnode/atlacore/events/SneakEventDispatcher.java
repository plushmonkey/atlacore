package com.plushnode.atlacore.events;

import com.plushnode.atlacore.events.PlayerToggleSneakEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;
import java.util.Map;

// Posts an event if any player toggles sneak. Is there no way to do this with existing Sponge events?
public class SneakEventDispatcher implements Runnable {
    private Map<org.spongepowered.api.entity.living.player.Player, Boolean> sneakMap = new HashMap<>();

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
        sneakMap.remove(event.getTargetEntity());
    }

    @Override
    public void run() {
        for (org.spongepowered.api.entity.living.player.Player player : Sponge.getServer().getOnlinePlayers()) {
            boolean isSneaking = player.get(Keys.IS_SNEAKING).orElse(false);
            Boolean wasSneaking = sneakMap.get(player);

            if (wasSneaking == null) {
                wasSneaking = false;
            }

            if (isSneaking != wasSneaking) {
                PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(player, isSneaking, null);
                Sponge.getEventManager().post(event);

                sneakMap.put(player, isSneaking);
            }
        }
    }
}
