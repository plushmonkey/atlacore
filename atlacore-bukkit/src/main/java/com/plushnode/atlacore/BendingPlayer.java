package com.plushnode.atlacore;

public class BendingPlayer extends BendingUser implements Player {
    public BendingPlayer(org.bukkit.entity.Player player) {
        super(player);
    }

    public org.bukkit.entity.Player getBukkitPlayer() {
        return (org.bukkit.entity.Player)entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BendingPlayer) {
            return getBukkitPlayer().equals(((BendingPlayer)obj).getBukkitPlayer());
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
}
