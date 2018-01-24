package com.plushnode.atlacore.entity.user;

import java.util.UUID;

public interface PlayerFactory {
    Player createPlayer(String name);
    Player createPlayer(UUID uuid);
}
