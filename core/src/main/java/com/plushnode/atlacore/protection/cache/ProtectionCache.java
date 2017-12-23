package com.plushnode.atlacore.protection.cache;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.ability.AbilityDescription;

import java.util.Optional;

public interface ProtectionCache {
    Optional<Boolean> canBuild(User user, AbilityDescription abilityDescription, Location location);
    void store(User user, AbilityDescription abilityDescription, Location location, boolean allowed);
    void reload();
}
