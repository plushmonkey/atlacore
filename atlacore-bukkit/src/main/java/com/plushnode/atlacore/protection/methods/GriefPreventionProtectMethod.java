package com.plushnode.atlacore.protection.methods;

import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.plushnode.atlacore.platform.LocationWrapper;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GriefPreventionProtectMethod implements ProtectMethod {
    public GriefPreventionProtectMethod(Plugin plugin) throws PluginNotFoundException {
        GriefPrevention griefPrevention = (GriefPrevention) plugin.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (griefPrevention == null)
            throw new PluginNotFoundException("GriefPrevention");
    }

    @Override
    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        if (!(user instanceof BukkitBendingPlayer)) return true;
        Player player = ((BukkitBendingPlayer)user).getBukkitPlayer();
        org.bukkit.Location bukkitLocation = ((LocationWrapper)location).getBukkitLocation();

        String reason = GriefPrevention.instance.allowBuild(player, bukkitLocation);
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(bukkitLocation, true, null);

        return reason == null || claim == null || claim.siegeData != null;
    }

    @Override
    public String getName() {
        return "GriefPrevention";
    }
}
