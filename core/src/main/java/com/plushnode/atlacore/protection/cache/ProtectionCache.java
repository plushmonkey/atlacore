package com.plushnode.atlacore.protection.cache;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;

import java.util.Optional;

public interface ProtectionCache {
    Optional<Boolean> canBuild(User user, AbilityDescription abilityDescription, Location location);
    void store(User user, AbilityDescription abilityDescription, Location location, boolean allowed);
    void reload();
}
