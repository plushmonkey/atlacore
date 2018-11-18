package com.plushnode.atlacore.platform;

import org.bukkit.inventory.ItemStack;

public class ItemSnapshotWrapper implements ItemSnapshot {
    private ItemStack item;

    public ItemSnapshotWrapper(ItemStack item) {
        this.item = item;
    }

    public ItemStack getBukkitItem() {
        return item;
    }
}
