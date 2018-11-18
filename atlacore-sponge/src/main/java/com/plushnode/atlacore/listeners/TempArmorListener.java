package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import com.plushnode.atlacore.util.TempArmor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import java.util.List;
import java.util.Optional;

public class TempArmorListener {
    @Listener(order = Order.EARLY)
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        org.spongepowered.api.entity.living.player.Player spongePlayer = event.getTargetEntity();
        Player player = Game.getPlayerService().getPlayerByUUID(spongePlayer.getUniqueId());

        if (player == null) return;

        Game.getTempArmorService().revert(player);
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent event, @First org.spongepowered.api.entity.living.player.Player spongePlayer) {
        Player player = Game.getPlayerService().getPlayerByUUID(spongePlayer.getUniqueId());

        if (player == null) return;

        // Reverting TempArmor before drop event fires will automatically handle the TempArmor drops.
        Game.getTempArmorService().revert(player);
    }

    @Listener
    // NOTE: ChangeInventoryEvent.Equipment seems to not fire.
    public void onInventoryChangeEvent(ChangeInventoryEvent event) {
        Inventory inventory = event.getTargetInventory();
        org.spongepowered.api.entity.living.player.Player spongePlayer = null;

        // I have no idea what the best way of doing this is. Sponge API is so fucking over-engineered to the point of not being usable.
        if (inventory instanceof CarriedInventory) {
            Optional result = ((CarriedInventory) inventory).getCarrier();

            if (result.isPresent()) {
                Object obj = result.get();

                if (obj instanceof org.spongepowered.api.entity.living.player.Player) {
                    spongePlayer = (org.spongepowered.api.entity.living.player.Player)obj;
                }
            }
        }

        if (spongePlayer == null) return;

        Player player = Game.getPlayerService().getPlayerByUUID(spongePlayer.getUniqueId());

        List<TempArmor> tempArmors = Game.getTempArmorService().getTempArmors(player);

        if (tempArmors.isEmpty()) return;

        for (SlotTransaction transaction : event.getTransactions()) {
            Slot slot = transaction.getSlot();

            // Seems like the equipment properties don't work? Default to using SlotIndex, but equipment properties would be better.
            Optional<SlotIndex> result = slot.getInventoryProperty(SlotIndex.class);

            if (!result.isPresent()) {
                continue;
            }

            Integer index = result.get().getValue();
            if (index == null) continue;

            int slotIndex = index;

            TempArmor.Slot tempArmorSlot = null;
            if (slotIndex == 5) {
                tempArmorSlot = TempArmor.Slot.Helmet;
            } else if (slotIndex == 6) {
                tempArmorSlot = TempArmor.Slot.Chestplate;
            } else if (slotIndex == 7) {
                tempArmorSlot = TempArmor.Slot.Leggings;
            } else if (slotIndex == 8) {
                tempArmorSlot = TempArmor.Slot.Boots;
            }

            if (tempArmorSlot == null) {
                continue;
            }

            for (TempArmor tempArmor : tempArmors) {
                Material tempType = tempArmor.getNewItem().getType();
                Material targetType = SpongeTypeUtil.adapt(transaction.getFinal().getType());

                if (tempArmor.getSlot() == tempArmorSlot && tempType != targetType) {
                    event.getTransactions().forEach(t -> {
                        t.setValid(false);
                    });

                    // This is such a hack, but Sponge API is annoying to work with.
                    Game.plugin.createTask(() -> {
                        for (TempArmor temp : Game.getTempArmorService().getTempArmors(player)) {
                            temp.refresh();
                        }
                    }, 1);

                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
