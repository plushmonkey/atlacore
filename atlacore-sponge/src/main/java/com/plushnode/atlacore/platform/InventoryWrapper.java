package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.material.SpongeMaterialUtil;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;

import java.util.Optional;

public class InventoryWrapper implements Inventory {
    private Player player;

    public InventoryWrapper(Player player) {
        this.player = player;
    }

    public Player getSpongePlayer() {
        return this.player;
    }

    @Override
    public boolean addItem(ItemStack item) {
        org.spongepowered.api.item.inventory.ItemStack spongeItem = SpongeTypeUtil.adapt(item);

        if (spongeItem != null) {
            player.getInventory().offer(spongeItem);
            return true;
        }

        return false;
    }

    @Override
    public void clear() {
        player.getInventory().clear();
    }

    @Override
    public boolean contains(ItemStack item) {
        org.spongepowered.api.item.inventory.ItemStack spongeItem = SpongeTypeUtil.adapt(item);

        return spongeItem != null && player.getInventory().contains(spongeItem);
    }

    @Override
    public boolean contains(Material itemType) {
        ItemType spongeType = SpongeMaterialUtil.toItemType(itemType);

        return spongeType != null && player.getInventory().contains(spongeType);
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        return containsAtLeast(item.getType(), amount);
    }

    @Override
    public boolean containsAtLeast(Material itemType, int amount) {
        ItemType spongeType = SpongeMaterialUtil.toItemType(itemType);

        if (spongeType == null) {
            return false;
        }

        org.spongepowered.api.item.inventory.ItemStack check = org.spongepowered.api.item.inventory.ItemStack.builder()
                .itemType(spongeType)
                .quantity(amount)
                .build();

        return player.getInventory().contains(check);
    }

    @Override
    public void removeAll(ItemStack item) {
        ItemType type = SpongeMaterialUtil.toItemType(item.getType());

        for (org.spongepowered.api.item.inventory.Inventory slot : player.getInventory().slots()) {
            Optional<org.spongepowered.api.item.inventory.ItemStack> current = slot.peek();

            if (current.isPresent()) {
                if (current.get().getType() == type && current.get().getQuantity() == item.getAmount()) {
                    slot.poll();
                }
            }
        }
    }

    @Override
    public void removeAll(Material itemType) {
        ItemType type = SpongeMaterialUtil.toItemType(itemType);

        for (org.spongepowered.api.item.inventory.Inventory slot : player.getInventory().slots()) {
            Optional<org.spongepowered.api.item.inventory.ItemStack> current = slot.peek();

            if (current.isPresent()) {
                if (current.get().getType() == type) {
                    slot.poll();
                }
            }
        }
    }

    @Override
    public boolean removeAmount(ItemStack item, int amount) {
        return removeAmount(item.getType(), amount);
    }

    @Override
    public boolean removeAmount(Material itemType, int amount) {
        ItemType type = SpongeMaterialUtil.toItemType(itemType);

        for (org.spongepowered.api.item.inventory.Inventory slot : player.getInventory().slots()) {
            Optional<org.spongepowered.api.item.inventory.ItemStack> current = slot.peek();

            if (current.isPresent()) {
                int quantity = current.get().getQuantity();

                if (current.get().getType() == type && quantity >= amount) {
                    if (quantity <= amount) {
                        slot.poll();
                        amount -= quantity;
                    } else {
                        current.get().setQuantity(quantity - amount);
                        amount = 0;
                    }
                }

                if (amount <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ItemStack getMainHand() {
        org.spongepowered.api.item.inventory.ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);

        if (item == null) {
            return null;
        }

        return SpongeTypeUtil.adapt(item);
    }

    @Override
    public ItemStack getOffHand() {
        org.spongepowered.api.item.inventory.ItemStack item = player.getItemInHand(HandTypes.OFF_HAND).orElse(null);

        if (item == null) {
            return null;
        }

        return SpongeTypeUtil.adapt(item);
    }

    @Override
    public void setMainHand(ItemStack item) {
        player.setItemInHand(HandTypes.MAIN_HAND, SpongeTypeUtil.adapt(item));
    }

    @Override
    public void setOffHand(ItemStack item) {
        player.setItemInHand(HandTypes.OFF_HAND, SpongeTypeUtil.adapt(item));
    }

    @Override
    public ItemStack[] getArmorContents() {
        ItemStack[] result = new ItemStack[4];

        result[0] = SpongeTypeUtil.adapt(player.getHelmet().orElse(null));
        result[1] = SpongeTypeUtil.adapt(player.getChestplate().orElse(null));
        result[2] = SpongeTypeUtil.adapt(player.getLeggings().orElse(null));
        result[3] = SpongeTypeUtil.adapt(player.getBoots().orElse(null));

        return result;
    }

    @Override
    public ItemStack getHelmet() {
        return SpongeTypeUtil.adapt(player.getHelmet().orElse(null));
    }

    @Override
    public ItemStack getChestplate() {
        return SpongeTypeUtil.adapt(player.getChestplate().orElse(null));
    }

    @Override
    public ItemStack getLeggings() {
        return SpongeTypeUtil.adapt(player.getLeggings().orElse(null));
    }

    @Override
    public ItemStack getBoots() {
        return SpongeTypeUtil.adapt(player.getBoots().orElse(null));
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        setHelmet(items[0]);
        setChestplate(items[1]);
        setLeggings(items[2]);
        setBoots(items[3]);
    }

    @Override
    public void setHelmet(ItemStack item) {
        player.setHelmet(SpongeTypeUtil.adapt(item));
    }

    @Override
    public void setChestplate(ItemStack item) {
        player.setChestplate(SpongeTypeUtil.adapt(item));
    }

    @Override
    public void setLeggings(ItemStack item) {
        player.setLeggings(SpongeTypeUtil.adapt(item));
    }

    @Override
    public void setBoots(ItemStack item) {
        player.setBoots(SpongeTypeUtil.adapt(item));
    }
}
