package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.TypeUtil;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

// Wraps a PlayerInventory only
public class InventoryWrapper implements Inventory {
    private PlayerInventory inventory;

    public InventoryWrapper(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    public PlayerInventory getBukkitInventory() {
        return this.inventory;
    }

    @Override
    public boolean addItem(ItemStack item) {
        return inventory.addItem(TypeUtil.adapt(item)).isEmpty();
    }

    @Override
    public boolean addItem(ItemSnapshot item) {
        return inventory.addItem(((ItemSnapshotWrapper)item).getBukkitItem()).isEmpty();
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public boolean contains(ItemStack item) {
        return inventory.contains(TypeUtil.adapt(item));
    }

    @Override
    public boolean contains(ItemSnapshot item) {
        org.bukkit.inventory.ItemStack itemStack = ((ItemSnapshotWrapper)item).getBukkitItem();

        return inventory.contains(itemStack);
    }

    @Override
    public boolean contains(Material itemType) {
        return inventory.contains(TypeUtil.adapt(itemType));
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        return inventory.containsAtLeast(TypeUtil.adapt(item), amount);
    }

    @Override
    public boolean containsAtLeast(ItemSnapshot item, int amount) {
        org.bukkit.inventory.ItemStack itemStack = ((ItemSnapshotWrapper)item).getBukkitItem();

        return inventory.containsAtLeast(itemStack, amount);
    }

    @Override
    public boolean containsAtLeast(Material itemType, int amount) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(TypeUtil.adapt(itemType), 1);

        return inventory.containsAtLeast(item, amount);
    }

    @Override
    public ItemStack getMainHand() {
        return TypeUtil.adapt(inventory.getItemInMainHand());
    }

    @Override
    public ItemStack getOffHand() {
        return TypeUtil.adapt(inventory.getItemInOffHand());
    }

    @Override
    public void setMainHand(ItemStack item) {
        inventory.setItemInMainHand(TypeUtil.adapt(item));
    }

    @Override
    public void setOffHand(ItemStack item) {
        inventory.setItemInOffHand(TypeUtil.adapt(item));
    }

    @Override
    public ItemStack[] getArmorContents() {
        org.bukkit.inventory.ItemStack[] items = inventory.getArmorContents();
        ItemStack[] result = new ItemStack[items.length];

        for (int i = 0; i < items.length; ++i) {
            result[i] = TypeUtil.adapt(items[i]);
        }

        return result;
    }

    @Override
    public ItemStack getHelmet() {
        return TypeUtil.adapt(inventory.getHelmet());
    }

    @Override
    public ItemStack getChestplate() {
        return TypeUtil.adapt(inventory.getChestplate());
    }

    @Override
    public ItemStack getLeggings() {
        return TypeUtil.adapt(inventory.getLeggings());
    }

    @Override
    public ItemStack getBoots() {
        return TypeUtil.adapt(inventory.getBoots());
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        org.bukkit.inventory.ItemStack[] armorItems = new org.bukkit.inventory.ItemStack[items.length];

        for (int i = 0; i < items.length; ++i) {
            armorItems[i] = TypeUtil.adapt(items[i]);
        }

        inventory.setArmorContents(armorItems);
    }

    @Override
    public void setHelmet(ItemStack item) {
        inventory.setHelmet(TypeUtil.adapt(item));
    }

    @Override
    public void setChestplate(ItemStack item) {
        inventory.setChestplate(TypeUtil.adapt(item));
    }

    @Override
    public void setLeggings(ItemStack item) {
        inventory.setLeggings(TypeUtil.adapt(item));
    }

    @Override
    public void setBoots(ItemStack item) {
        inventory.setBoots(TypeUtil.adapt(item));
    }

    @Override
    public ItemSnapshot getHelmetSnapshot() {
        return new ItemSnapshotWrapper(inventory.getHelmet());
    }

    @Override
    public ItemSnapshot getChestplateSnapshot() {
        return new ItemSnapshotWrapper(inventory.getChestplate());
    }

    @Override
    public ItemSnapshot getLeggingsSnapshot() {
        return new ItemSnapshotWrapper(inventory.getLeggings());
    }

    @Override
    public ItemSnapshot getBootsSnapshot() {
        return new ItemSnapshotWrapper(inventory.getBoots());
    }

    @Override
    public void setHelmet(ItemSnapshot item) {
        inventory.setHelmet(((ItemSnapshotWrapper)item).getBukkitItem());
    }

    @Override
    public void setChestplate(ItemSnapshot item) {
        inventory.setChestplate(((ItemSnapshotWrapper)item).getBukkitItem());
    }

    @Override
    public void setLeggings(ItemSnapshot item) {
        inventory.setLeggings(((ItemSnapshotWrapper)item).getBukkitItem());
    }

    @Override
    public void setBoots(ItemSnapshot item) {
        inventory.setBoots(((ItemSnapshotWrapper)item).getBukkitItem());
    }

    @Override
    public void removeAll(ItemStack item) {
        inventory.remove(TypeUtil.adapt(item));
    }

    @Override
    public void removeAll(Material itemType) {
        inventory.remove(new org.bukkit.inventory.ItemStack(TypeUtil.adapt(itemType)));
    }

    @Override
    public boolean removeAmount(ItemStack item, int amount) {
        return removeAmount(item.getType(), amount);
    }

    @Override
    public boolean removeAmount(ItemSnapshot item, int amount) {
        org.bukkit.inventory.ItemStack bukkitItem = ((ItemSnapshotWrapper)item).getBukkitItem();
        org.bukkit.inventory.ItemStack itemClone = bukkitItem.clone();

        itemClone.setAmount(1);

        for (org.bukkit.inventory.ItemStack i : inventory.getContents()) {
            if (i == null) continue;

            org.bukkit.inventory.ItemStack currentClone = i.clone();
            currentClone.setAmount(1);

            if (currentClone.equals(itemClone)) {
                if (i.getAmount() <= amount) {
                    amount -= i.getAmount();
                    inventory.removeItem(i);
                } else {
                    i.setAmount(i.getAmount() - amount);
                    amount = 0;
                }
            }

            if (amount <= 0) {
                break;
            }
        }

        return amount <= 0;
    }

    @Override
    public boolean removeAmount(Material itemType, int amount) {
        org.bukkit.Material material = TypeUtil.adapt(itemType);

        for (org.bukkit.inventory.ItemStack i : inventory.getContents()) {
            if (i != null && i.getType() == material) {
                if (i.getAmount() == amount) {
                    Map<Integer, ? extends org.bukkit.inventory.ItemStack> remaining = inventory.removeItem(i);

                    if (!remaining.isEmpty()) {
                        org.bukkit.inventory.ItemStack offhand = inventory.getItemInOffHand();

                        if (offhand != null && offhand.getType() == material && offhand.getAmount() == amount) {
                            inventory.setItemInOffHand(null);
                        }
                    }
                } else if (i.getAmount() > amount) {
                    i.setAmount(i.getAmount() - amount);
                }

                return true;
            }
        }

        return false;
    }
}
