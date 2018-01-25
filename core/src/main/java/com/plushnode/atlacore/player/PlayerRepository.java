package com.plushnode.atlacore.player;

import com.plushnode.atlacore.platform.Player;

import java.util.UUID;

public interface PlayerRepository {
    Player getPlayerByUUID(UUID uuid);
    Player getPlayerByName(String name);
    Player createPlayer(UUID uuid, String name);
    void savePlayer(Player player);
}
