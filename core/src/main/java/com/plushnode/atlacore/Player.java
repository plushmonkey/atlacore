package com.plushnode.atlacore;

public interface Player extends User {
    boolean isOnline();
    boolean isSneaking();
    GameMode getGameMode();
}
