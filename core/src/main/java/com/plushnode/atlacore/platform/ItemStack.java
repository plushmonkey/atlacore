package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Material;

public class ItemStack {
    private Material type;
    private int amount;

    public ItemStack(Material type) {
        this.type = type;
        this.amount = 1;
    }

    public ItemStack(Material type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public Material getType() {
        return this.type;
    }

    public int getAmount() {
        return this.amount;
    }
}
