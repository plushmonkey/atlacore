package com.plushnode.atlacore.protection.methods;

import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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
        org.bukkit.Location bukkitLocation = ((LocationWrapper)location).getBukkitLocation();

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        if (user instanceof com.plushnode.atlacore.platform.Player) {
            Player player = ((BukkitBendingPlayer)user).getBukkitPlayer();

            boolean hasBypass = WorldGuard.getInstance()
                    .getPlatform()
                    .getSessionManager()
                    .hasBypass(worldGuard.wrapPlayer(player), BukkitAdapter.adapt(bukkitLocation.getWorld()));

            return hasBypass || query.testState(BukkitAdapter.adapt(bukkitLocation), worldGuard.wrapPlayer(player), Flags.BUILD);
        }

        // Query WorldGuard to see if a non-member (entity) can build in a region.
        return query.testState(BukkitAdapter.adapt(bukkitLocation), (list) -> Association.NON_MEMBER, Flags.BUILD);
    }

    @Override
    public String getName() {
        return "WorldGuard";
    }
}
