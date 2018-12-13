package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.board.BendingBoard;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.air.AirBurst;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.game.ability.air.AirSpout;
import com.plushnode.atlacore.game.ability.air.passives.GracefulDescent;
import com.plushnode.atlacore.game.ability.earth.passives.DensityShift;
import com.plushnode.atlacore.game.ability.fire.Combustion;
import com.plushnode.atlacore.game.ability.fire.FireBurst;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.ability.fire.HeatControl;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlast;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlaze;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.util.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class PlayerListener implements Listener {
    // TODO: Move into appropriate place
    public static Map<String, BendingBoard> boards = new HashMap<>();
    private Task boardTask = null;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Create the player on the next tick so createPlayer can find the Bukkit player.
        Game.plugin.createTask(() -> {
            Game.getPlayerService().createPlayer(player.getUniqueId(), player.getName(), (p) -> {
                if (p == null || !p.isOnline()) {
                    // This can happen if the player logs off before the player is created.
                    return;
                }

                boards.put(p.getName(), new BendingBoard(p));

                if (boardTask == null) {
                    boardTask = Game.plugin.createTaskTimer(() -> {
                        for (BendingBoard board : boards.values()) {
                            board.update();
                        }
                    }, 5, 5);
                }

                Game.getAbilityInstanceManager().createPassives(p);
            });
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogout(PlayerQuitEvent event) {
        com.plushnode.atlacore.platform.Player player =
                Game.getPlayerService().getPlayerByName(event.getPlayer().getName());

        if (player == null) return;

        Game.getActivationController().onPlayerLogout(player);
        boards.remove(player.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (user == null) return;

        if (!Game.getActivationController().onFallDamage(user)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireTickDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE && event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (user == null) return;

        if (!Game.getActivationController().onFireTickDamage(user)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = Game.getPlayerService().getPlayerByName(event.getPlayer().getName());

        if (user == null) return;

        Vector3D velocity = TypeUtil.adapt(event.getTo().clone().subtract(event.getFrom()).toVector());

        Game.getActivationController().onUserMove(user, velocity);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (user == null) return;

        if (event.getHand() == EquipmentSlot.HAND) {
            boolean rightClickAir = event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR;

            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            {
                Game.getActivationController().onUserInteract(user, rightClickAir);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (user == null) return;

        Game.getActivationController().onUserInteractEntity(user);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (user == null) return;

        Game.getActivationController().onUserSneak(user, event.isSneaking());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (user == null) return;

        Game.getActivationController().onUserSwing(user);
    }
}
