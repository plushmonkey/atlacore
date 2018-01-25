package com.plushnode.atlacore.player;

import com.plushnode.atlacore.platform.Player;

import java.util.UUID;

public interface PlayerFactory {
    Player createPlayer(String name);
    Player createPlayer(UUID uuid);
}
