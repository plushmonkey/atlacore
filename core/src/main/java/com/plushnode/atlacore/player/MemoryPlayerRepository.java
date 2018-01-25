package com.plushnode.atlacore.player;

import com.plushnode.atlacore.platform.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryPlayerRepository implements PlayerRepository {
    private Map<UUID, Player> store = new HashMap<>();
    private PlayerFactory factory;

    public MemoryPlayerRepository(PlayerFactory factory) {
        this.factory = factory;
    }

    @Override
    public synchronized Player getPlayerByUUID(UUID uuid) {
        return store.get(uuid);
    }

    @Override
    public synchronized Player getPlayerByName(String name) {
        return store.values().stream().filter((p) -> p.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    @Override
    public synchronized Player createPlayer(UUID uuid, String name) {
        Player player = factory.createPlayer(name);

        Player current = store.get(uuid);
        // Copy over existing player
        if (current != null) {
            player.getElements().addAll(current.getElements());

            for (int slotIndex = 1; slotIndex <= 9; ++slotIndex) {
                player.setSlotAbility(slotIndex, current.getSlotAbility(slotIndex));
            }
        }

        store.put(uuid, player);
        return player;
    }

    @Override
    public void savePlayer(Player player) {
        // In memory store does nothing.
    }
}
