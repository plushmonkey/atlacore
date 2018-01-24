package com.plushnode.atlacore.entity.user;

import com.plushnode.atlacore.GameMode;
import com.plushnode.atlacore.command.CommandSender;
import com.plushnode.atlacore.entity.user.User;

public interface Player extends User, CommandSender {
    boolean isOnline();
    boolean isSneaking();
    GameMode getGameMode();
    int getHeldItemSlot();
}
