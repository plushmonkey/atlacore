package com.plushnode.atlacore.protection.methods;

import com.plushnode.atlacore.BukkitBendingPlayer;
import com.plushnode.atlacore.Location;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.plushnode.atlacore.wrappers.LocationWrapper;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardProtectMethod implements ProtectMethod {
    private WorldGuardPlugin worldGuard;

    public WorldGuardProtectMethod(Plugin plugin) throws PluginNotFoundException {
        worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if (worldGuard == null)
            throw new PluginNotFoundException("WorldGuard");
    }

    @Override
    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        if (!(user instanceof BukkitBendingPlayer)) return true;
        Player player = ((BukkitBendingPlayer)user).getBukkitPlayer();
        org.bukkit.Location bukkitLocation = ((LocationWrapper)location).getBukkitLocation();

        return worldGuard.canBuild(player, bukkitLocation);
    }

    @Override
    public String getName() {
        return "WorldGuard";
    }
}
