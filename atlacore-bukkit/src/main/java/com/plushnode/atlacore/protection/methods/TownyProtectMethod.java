package com.plushnode.atlacore.protection.methods;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.eventwar.WarUtil;
import com.palmergames.bukkit.towny.war.flagwar.FlagWar;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.plushnode.atlacore.platform.LocationWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class TownyProtectMethod implements ProtectMethod {
    private Towny towny;

    public TownyProtectMethod(Plugin plugin) throws PluginNotFoundException {
        this.towny = (Towny) plugin.getServer().getPluginManager().getPlugin("Towny");

        if (this.towny == null)
            throw new PluginNotFoundException("Towny");
    }

    @Override
    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        // Surround with try-catch because Towny is garbage and might throw random exceptions.
        try {
            TownyWorld world = com.palmergames.bukkit.towny.TownyUniverse.getInstance().getWorldMap().get(user.getWorld().getName());

            if (world != null && !world.isUsingTowny()) {
                // Exit early if this world isn't being protected by Towny.
                return true;
            }
        } catch (Exception e) {
            //
        }

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
            canBuild = PlayerCacheUtil.getCachePermission(player, bukkitLocation, Material.STONE, TownyPermission.ActionType.BUILD);
        } catch (NullPointerException e) {

        }

        if (!canBuild && TownyAPI.getInstance().isWarTime()) {
            try {
                PlayerCache cache = this.towny.getCache(player);
                PlayerCache.TownBlockStatus status = cache.getStatus();

                if (status == PlayerCache.TownBlockStatus.ENEMY && !WarUtil.isPlayerNeutral(player)) {
                    TownyWorld townyWorld;

                    try {
                        townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(player.getWorld().getName());

                        WorldCoord worldCoord = new WorldCoord(townyWorld.getName(), Coord.parseCoord(bukkitLocation));
                        FlagWar.callAttackCellEvent(this.towny, player, bukkitLocation.getBlock(), worldCoord);
                    } catch (Exception e) {

                    }

                    canBuild = true;
                } else if (status == PlayerCache.TownBlockStatus.WARZONE) {
                    canBuild = true;
                }
            } catch (Exception e) {

            }
        }

        return canBuild;
    }

    // Checks if the location is in a town block.
    private boolean canEntityBuild(User user, Location location) {
        Collection<TownBlock> blocks = TownyUniverse.getInstance().getDataSource().getAllTownBlocks();

        int x = (int)Math.floor(location.getX() / 16.0);
        int z = (int)Math.floor(location.getZ() / 16.0);

        TownyWorld world;
        try {
            world = TownyUniverse.getInstance().getDataSource().getWorld(location.getWorld().getName());
        } catch (Exception e) {
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
