package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.command.CommandSender;

public interface Player extends User, CommandSender {
    boolean isOnline();
    boolean isSneaking();
    GameMode getGameMode();
    int getHeldItemSlot();
    void setHeldItemSlot(int slot);
}
