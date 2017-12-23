package com.plushnode.atlacore.protection;

import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.ability.AbilityDescription;

import java.util.ArrayList;
import java.util.List;

// Determines whether or not someone can build in some region.
// There should be global RegionProtection and world RegionProtections.
// The global RegionProtection will apply ProtectMethods everywhere.
// The world RegionProtections will apply to a single world.
public class RegionProtection {
    private List<ProtectMethod> protectionMethods = new ArrayList<>();

    public void addProtectMethod(ProtectMethod method) {
        protectionMethods.add(method);
    }

    public void removeProtectMethod(ProtectMethod method) {
        protectionMethods.remove(method);
    }

    public void clearProtectMethods() {
        protectionMethods.clear();
    }

    public boolean hasProtectMethod(String method) {
        for (ProtectMethod protection : protectionMethods) {
            if (protection.getName().equalsIgnoreCase(method)) return true;
        }

        return false;
    }

    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        for (ProtectMethod method : protectionMethods) {
            if (!method.canBuild(user, abilityDescription, location))
                return false;
        }

        return true;
    }
}
