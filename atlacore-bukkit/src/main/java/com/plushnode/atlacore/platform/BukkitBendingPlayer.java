package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.ability.AbilityDescription;

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
    public GameMode getGameMode() {
        return GameMode.values()[getBukkitPlayer().getGameMode().ordinal()];
    }

    @Override
    public int getHeldItemSlot() {
        return getBukkitPlayer().getInventory().getHeldItemSlot();
    }

    @Override
    public AbilityDescription getSelectedAbility() {
        int slot = getBukkitPlayer().getInventory().getHeldItemSlot() + 1;
        return getSlotAbility(slot);
    }

    @Override
    public void sendMessage(String message) {
        getBukkitPlayer().sendMessage(message);
    }

    @Override
    public String getName() {
        return getBukkitPlayer().getName();
    }

    @Override
    public boolean getAllowFlight() {
        return getBukkitPlayer().getAllowFlight();
    }

    @Override
    public boolean isFlying() {
        return getBukkitPlayer().isFlying();
    }

    @Override
    public void setAllowFlight(boolean allow) {
        getBukkitPlayer().setAllowFlight(allow);
    }

    @Override
    public void setFlying(boolean flying) {
        getBukkitPlayer().setFlying(flying);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getBukkitPlayer().hasPermission(permission);
    }
}
