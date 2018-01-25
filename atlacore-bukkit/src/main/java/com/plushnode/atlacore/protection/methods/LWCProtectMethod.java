package com.plushnode.atlacore.protection.methods;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.plushnode.atlacore.platform.BukkitBendingPlayer;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.protection.PluginNotFoundException;
import com.plushnode.atlacore.protection.ProtectMethod;
import com.plushnode.atlacore.platform.LocationWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LWCProtectMethod implements ProtectMethod {
    private LWCPlugin lwcPlugin;

    public LWCProtectMethod(Plugin plugin) throws PluginNotFoundException {
        this.lwcPlugin = (LWCPlugin)plugin.getServer().getPluginManager().getPlugin("LWC");
        if (this.lwcPlugin == null)
            throw new PluginNotFoundException("LWC");
    }

    @Override
    public boolean canBuild(User user, AbilityDescription abilityDescription, Location location) {
        if (!(user instanceof BukkitBendingPlayer)) return true;
        Player player = ((BukkitBendingPlayer)user).getBukkitPlayer();
        org.bukkit.Location bukkitLocation = ((LocationWrapper)location).getBukkitLocation();

        LWC lwc = lwcPlugin.getLWC();
        Protection protection = lwc.getProtectionCache().getProtection(bukkitLocation.getBlock());

        return protection == null || lwc.canAccessProtection(player, protection);
    }

    @Override
    public String getName() {
        return "LWC";
    }
}
