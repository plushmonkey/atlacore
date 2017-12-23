package com.plushnode.atlacore.protection;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.ability.AbilityDescription;

public interface ProtectMethod {
    boolean canBuild(User user, AbilityDescription abilityDescription, Location location);
    String getName();
}
