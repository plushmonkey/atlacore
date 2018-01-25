package com.plushnode.atlacore.protection;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;

public interface ProtectMethod {
    boolean canBuild(User user, AbilityDescription abilityDescription, Location location);
    String getName();
}
