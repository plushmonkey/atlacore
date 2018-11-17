package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Material;

public interface Inventory {
    boolean addItem(ItemStack item);
    void clear();

    // Exact match.
    boolean contains(ItemStack item);
    boolean contains(Material itemType);

    // Checks if inventory contains at least some amount of item.
    boolean containsAtLeast(ItemStack item, int amount);
    boolean containsAtLeast(Material itemType, int amount);

    // Remove all stacks that match item.
    void removeAll(ItemStack item);
    // Remove all stacks that match item type.
    void removeAll(Material itemType);

    // Remove some amount of item from the inventory.
    boolean removeAmount(ItemStack item, int amount);
    // Remove some amount of item from the inventory.
    boolean removeAmount(Material itemType, int amount);

    ItemStack getMainHand();
    ItemStack getOffHand();

    void setMainHand(ItemStack item);
    void setOffHand(ItemStack item);

    ItemStack[] getArmorContents();
    ItemStack getHelmet();
    ItemStack getChestplate();
    ItemStack getLeggings();
    ItemStack getBoots();

    void setArmorContents(ItemStack[] items);
    void setHelmet(ItemStack item);
    void setChestplate(ItemStack item);
    void setLeggings(ItemStack item);
    void setBoots(ItemStack item);
}
