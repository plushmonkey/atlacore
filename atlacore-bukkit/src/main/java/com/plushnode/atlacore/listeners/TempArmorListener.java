package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.events.armor.ArmorEquipEvent;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.ItemSnapshotWrapper;
import com.plushnode.atlacore.util.TempArmor;
import com.plushnode.atlacore.util.TypeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TempArmorListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onArmorEquipEvent(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        com.plushnode.atlacore.platform.Player gamePlayer = Game.getPlayerService().getPlayerByUUID(player.getUniqueId());

        if (gamePlayer == null) return;

        TempArmor.Slot slot = TempArmor.Slot.values()[event.getType().ordinal()];
        List<TempArmor> tempArmors = Game.getTempArmorService().getTempArmors(gamePlayer);

        for (TempArmor tempArmor : tempArmors) {
            if (tempArmor.getSlot() == slot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        com.plushnode.atlacore.platform.Player gamePlayer = Game.getPlayerService().getPlayerByUUID(player.getUniqueId());

        if (gamePlayer != null) {
            List<TempArmor> tempArmors = Game.getTempArmorService().getTempArmors(gamePlayer);

            if (!tempArmors.isEmpty()) {
                List<ItemStack> newDrops = new ArrayList<>();

                for (ItemStack drop : event.getDrops()) {
                    boolean filtered = false;
                    TempArmor filteredArmor = null;

                    for (TempArmor tempArmor : tempArmors) {
                        Material material = TypeUtil.adapt(tempArmor.getNewItem().getType());

                        if (material == drop.getType()) {
                            // Add in the snapshot item to the new drops list and then mark it for removal.
                            newDrops.add(((ItemSnapshotWrapper) tempArmor.getSnapshot()).getBukkitItem());
                            filteredArmor = tempArmor;
                            filtered = true;
                            break;
                        }
                    }

                    // Remove from the tempArmors list so multiple items of the same type aren't all filtered.
                    if (filteredArmor != null) {
                        tempArmors.remove(filteredArmor);
                    }

                    if (!filtered) {
                        newDrops.add(drop);
                    }
                }

                event.getDrops().clear();
                event.getDrops().addAll(newDrops);

                Game.getTempArmorService().revert(gamePlayer);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        com.plushnode.atlacore.platform.Player player = Game.getPlayerService().getPlayerByName(event.getPlayer().getName());

        if (player == null) return;

        Game.getTempArmorService().revert(player);
    }
}
