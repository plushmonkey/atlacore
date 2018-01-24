package com.plushnode.atlacore.entity.user;

import java.util.UUID;

public interface PlayerRepository {
    Player getPlayerByUUID(UUID uuid);
    Player getPlayerByName(String name);
    Player createPlayer(UUID uuid, String name);
    void savePlayer(Player player);
}
