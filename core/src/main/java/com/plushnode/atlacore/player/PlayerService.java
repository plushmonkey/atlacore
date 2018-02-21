package com.plushnode.atlacore.player;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.Player;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerService {
    private Executor executor;
    private PlayerRepository repository;
    private Map<String, Player> playerCache = new HashMap<>();

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
        this.executor = Executors.newFixedThreadPool(3);
    }

    public void reload(PlayerRepository repository) {
        List<Player> players = new ArrayList<>(playerCache.values());

        this.repository = repository;
        // Clear cache and reload all of them back into cache.
        playerCache.clear();
        for (Player player : players) {
            Player newPlayer = getPlayerByUUID(player.getUniqueId());
            if (newPlayer != null) {
                // Copy over previous conditionals for this player.
                newPlayer.setBendingConditional(player.getBendingConditional());
            }
        }
    }

    public Player getPlayerByUUID(UUID uuid) {
        Player player = playerCache.values().stream().filter((p) -> p.getUniqueId().equals(uuid)).findAny().orElse(null);

        if (player == null) {
            player = repository.getPlayerByUUID(uuid);
            if (player != null) {
                playerCache.put(player.getName().toLowerCase(), player);
            }
        }

        return player;
    }

    public Player getPlayerByName(String name) {
        Player player = playerCache.get(name.toLowerCase());

        if (player == null) {
            player = repository.getPlayerByName(name);
            if (player != null) {
                playerCache.put(name.toLowerCase(), player);
            }
        }

        return player;
    }

    public List<Player> getOnlinePlayers() {
        return playerCache.values().stream().filter(Player::isOnline).collect(Collectors.toList());
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
            if (player != null) {
                playerCache.put(name.toLowerCase(), player);
                Game.plugin.getEventBus().postBendingPlayerCreateEvent(player);
            }
            Game.plugin.createTask(() -> callback.accept(player), 0);
        });
    }

    public void savePlayer(Player player, Consumer<Player> callback) {
        executor.execute(() -> {
            repository.savePlayer(player);
            Game.plugin.createTask(() -> callback.accept(player), 0);
        });
    }

    public void saveSlot(Player player, int slotIndex) {
        executor.execute(() -> repository.saveSlot(player, slotIndex));
    }

    public void saveSlots(Player player) {
        executor.execute(() -> repository.saveSlots(player));
    }

    public void saveElements(Player player) {
        executor.execute(() -> repository.saveElements(player));
    }
}
