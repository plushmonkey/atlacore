package com.plushnode.atlacore;

import com.plushnode.atlacore.ability.AbilityDescription;

public class BukkitBendingPlayer extends BukkitBendingUser implements Player {
    public BukkitBendingPlayer(org.bukkit.entity.Player player) {
        super(player);
    }

    public org.bukkit.entity.Player getBukkitPlayer() {
        return (org.bukkit.entity.Player)entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BukkitBendingPlayer) {
            return getBukkitPlayer().equals(((BukkitBendingPlayer)obj).getBukkitPlayer());
        }
        return getBukkitPlayer().equals(obj);
    }

    @Override
    public int hashCode() {
        return getBukkitPlayer().hashCode();
    }

    @Override
    public boolean isOnline() {
        return getBukkitPlayer().isOnline();
    }

    @Override
    public boolean isSneaking() {
        return getBukkitPlayer().isSneaking();
    }

    @Override
    public AbilityDescription getSelectedAbility() {
        int slot = getBukkitPlayer().getInventory().getHeldItemSlot() + 1;
        return getSlotAbility(slot);
    }
}
