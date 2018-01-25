package com.plushnode.atlacore.protection.methods;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.engine.EngineMain;
import com.massivecraft.massivecore.ps.PS;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.plushnode.atlacore.platform.BlockWrapper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FactionsProtectMethod implements ProtectMethod {
    public FactionsProtectMethod(Plugin plugin) throws PluginNotFoundException {
        Factions factions = (Factions) plugin.getServer().getPluginManager().getPlugin("Factions");

        if (factions == null)
            throw new PluginNotFoundException("Factions");
    }

    @Override
    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        if (!(user instanceof BukkitBendingPlayer)) return true;
        Player player = ((BukkitBendingPlayer)user).getBukkitPlayer();
        Block block = ((BlockWrapper)location.getBlock()).getBukkitBlock();

        return EngineMain.canPlayerBuildAt(player, PS.valueOf(block), false);
    }

    @Override
    public String getName() {
        return "Factions";
    }
}
