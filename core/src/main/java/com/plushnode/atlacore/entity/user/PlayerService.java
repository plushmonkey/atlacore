package com.plushnode.atlacore.entity.user;

import com.plushnode.atlacore.Game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PlayerService {
    private Executor executor;
    private PlayerRepository repository;
    private Map<String, Player> playerCache = new HashMap<>();

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
        this.executor = Executors.newFixedThreadPool(3);
    }

    public Player getPlayerByUUID(UUID uuid) {
        Player player = playerCache.values().stream().filter((p) -> p.getUniqueId().equals(uuid)).findAny().orElse(null);

        if (player == null) {
            player = repository.getPlayerByUUID(uuid);
            if (player != null) {
                playerCache.put(player.getName(), player);
            }
        }

        return player;
    }

    public Player getPlayerByName(String name) {
        Player player = playerCache.get(name);

        if (player == null) {
            player = repository.getPlayerByName(name);
            playerCache.put(name, player);
        }

        return player;
    }

    public void invalidatePlayer(Player player) {
        for (Iterator<Map.Entry<String, Player>> iterator = playerCache.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Player> entry = iterator.next();

            if (entry.getValue().equals(player)) {
                iterator.remove();
                return;
            }
        }
    }

    public void createPlayer(UUID uuid, String name, Consumer<Player> callback) {
        executor.execute(() -> {
            Player player = repository.createPlayer(uuid, name);
            Game.plugin.createTask(() -> callback.accept(player), 0);
        });
    }

    public void savePlayer(Player player, Consumer<Player> callback) {
        executor.execute(() -> {
            repository.savePlayer(player);
            Game.plugin.createTask(() -> callback.accept(player), 0);
        });
    }
}
