package com.plushnode.atlacore.platform;

import org.spongepowered.api.item.inventory.ItemStack;

public class ItemSnapshotWrapper implements ItemSnapshot {
    private ItemStack item;

    public ItemSnapshotWrapper(ItemStack item) {
        this.item = item;
    }

    public ItemStack getSpongeItem() {
        return item;
    }
}
