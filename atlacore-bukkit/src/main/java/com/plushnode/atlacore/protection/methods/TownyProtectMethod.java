package com.plushnode.atlacore.protection.methods;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.plushnode.atlacore.platform.LocationWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TownyProtectMethod implements ProtectMethod {
    private Towny towny;

    public TownyProtectMethod(Plugin plugin) throws PluginNotFoundException {
        this.towny = (Towny) plugin.getServer().getPluginManager().getPlugin("Towny");

        if (this.towny == null)
            throw new PluginNotFoundException("Towny");
    }

    @Override
    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        if (user instanceof com.plushnode.atlacore.platform.Player) {
            return canPlayerBuild(user, location);
        }

        return canEntityBuild(user, location);
    }

    private boolean canPlayerBuild(User user, Location location) {
        Player player = ((BukkitBendingPlayer)user).getBukkitPlayer();
        org.bukkit.Location bukkitLocation = ((LocationWrapper)location).getBukkitLocation();

        boolean canBuild = true;

        try {
            canBuild = PlayerCacheUtil.getCachePermission(player, bukkitLocation, 3, (byte) 0, TownyPermission.ActionType.BUILD);
        } catch (NullPointerException e) {

        }

        if (!canBuild) {
            try {
                PlayerCache cache = this.towny.getCache(player);
                PlayerCache.TownBlockStatus status = cache.getStatus();

                if (status == PlayerCache.TownBlockStatus.ENEMY && TownyWarConfig.isAllowingAttacks()) {
                    TownyWorld townyWorld;

                    try {
                        townyWorld = TownyUniverse.getDataSource().getWorld(player.getWorld().getName());
                        WorldCoord worldCoord = new WorldCoord(townyWorld.getName(), Coord.parseCoord(bukkitLocation));
                        TownyWar.callAttackCellEvent(this.towny, player, bukkitLocation.getBlock(), worldCoord);
                    } catch (Exception e) {

                    }
                    return false;
                } else if (status == PlayerCache.TownBlockStatus.WARZONE) {

                } else {
                    return false;
                }
            } catch (Exception e) {

            }
        }

        return true;
    }

    // Checks if the location is in a town block.
    private boolean canEntityBuild(User user, Location location) {
        List<TownBlock> blocks = TownyUniverse.getDataSource().getAllTownBlocks();

        int x = (int)Math.floor(location.getX() / 16.0);
        int z = (int)Math.floor(location.getZ() / 16.0);

        TownyWorld world;
        try {
            world = TownyUniverse.getDataSource().getWorld(location.getWorld().getName());
        } catch (NotRegisteredException e) {
            return true;
        }

        TownBlock block = new TownBlock(x, z, world);

        return !blocks.contains(block);
    }

    @Override
    public String getName() {
        return "Towny";
    }
}
