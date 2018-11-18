package com.plushnode.atlacore.util;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.ItemSnapshot;
import com.plushnode.atlacore.platform.ItemStack;
import com.plushnode.atlacore.platform.User;

public class TempArmor {
    private User user;
    private ItemSnapshot snapshot;
    private ItemStack newItem;
    private Slot slot;
    private Task task;

    TempArmor(User user, ItemStack newItem, Slot slot, long duration) {
        this.user = user;
        this.newItem = newItem;
        this.slot = slot;
        long ticks = duration / 50;

        task = Game.plugin.createTask(() -> Game.getTempArmorService().revert(user, slot), ticks);

        switch (slot) {
            case Helmet:
                this.snapshot = this.user.getInventory().getHelmetSnapshot();
                user.getInventory().setHelmet(newItem);
            break;
            case Chestplate:
                this.snapshot = this.user.getInventory().getChestplateSnapshot();
                user.getInventory().setChestplate(newItem);
            break;
            case Leggings:
                this.snapshot = this.user.getInventory().getLeggingsSnapshot();
                user.getInventory().setLeggings(newItem);
            break;
            case Boots:
                this.snapshot = this.user.getInventory().getBootsSnapshot();
                user.getInventory().setBoots(newItem);
            break;
        }
    }

    public void refresh() {
        switch (slot) {
            case Helmet:
                user.getInventory().setHelmet(newItem);
            break;
            case Chestplate:
                user.getInventory().setChestplate(newItem);
            break;
            case Leggings:
                user.getInventory().setLeggings(newItem);
            break;
            case Boots:
                user.getInventory().setBoots(newItem);
            break;
        }
    }

    public User getUser() {
        return this.user;
    }

    public Slot getSlot() {
        return this.slot;
    }

    public ItemSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(ItemSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public ItemStack getNewItem() {
        return this.newItem;
    }

    public Task getTask() {
        return task;
    }

    void revert() {
        switch (slot) {
            case Helmet:
                this.user.getInventory().setHelmet(snapshot);
            break;
            case Chestplate:
                this.user.getInventory().setChestplate(snapshot);
            break;
            case Leggings:
                this.user.getInventory().setLeggings(snapshot);
            break;
            case Boots:
                this.user.getInventory().setBoots(snapshot);
            break;
        }
    }

    public enum Slot {
        Helmet,
        Chestplate,
        Leggings,
        Boots
    }
}
