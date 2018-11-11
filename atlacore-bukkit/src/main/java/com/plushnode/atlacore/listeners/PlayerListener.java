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
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.util.WorldUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

// NOTE: test code.
public class PlayerListener implements Listener {
    // TODO: Move into appropriate place
    public static Map<String, BendingBoard> boards = new HashMap<>();
    private Task boardTask = null;

    private boolean activateAbility(User user, ActivationMethod method) {
        AbilityDescription abilityDescription = user.getSelectedAbility();

        if (abilityDescription == null) return false;
        if (!abilityDescription.isActivatedBy(method)) return false;
        if (!user.canBend(abilityDescription)) return false;

        Ability ability = abilityDescription.createAbility();

        if (ability.activate(user, method)) {
            Game.addAbility(user, ability);
        } else {
            return false;
        }

        return true;
    }

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

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        com.plushnode.atlacore.platform.Player player =
                Game.getPlayerService().getPlayerByName(event.getPlayer().getName());

        if (player == null) return;

        Game.getAttributeSystem().clearModifiers(player);

        Game.getPlayerService().savePlayer(player, (p) -> {
            Game.info(p.getName() + " saved to database.");
        });

        Game.getPlayerService().invalidatePlayer(player);

        boards.remove(player.getName());

        Game.getAbilityInstanceManager().clearPassives(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();

        User user = Game.getPlayerService().getPlayerByName(player.getName());

        activateAbility(user, ActivationMethod.Fall);

        if (user.hasElement(Elements.AIR) && GracefulDescent.isGraceful(user)) {
            event.setCancelled(true);
        }

        if (user.hasElement(Elements.EARTH) && DensityShift.isSoftened(user)) {
            Block block = user.getLocation().getBlock().getRelative(BlockFace.DOWN);
            Location location = block.getLocation().add(0.5, 0.5, 0.5);
            DensityShift.softenArea(user, location);
            event.setCancelled(true);
        }

        if (Flight.hasFlight(user)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireTickDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE && event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (!HeatControl.canBurn(user)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = Game.getPlayerService().getPlayerByName(event.getPlayer().getName());

        if (user == null) return;

        Vector3D velocity = TypeUtil.adapt(event.getTo().clone().subtract(event.getFrom()).toVector());

        AirSpout.handleMovement(user, velocity);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (event.getHand() == EquipmentSlot.HAND) {
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                Game.getSequenceService().registerAction(user, Action.Interact);
            } else if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                Game.getSequenceService().registerAction(user, Action.InteractBlock);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        Game.getSequenceService().registerAction(user, Action.InteractEntity);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        Game.getSequenceService().registerAction(user, event.isSneaking() ? Action.Sneak : Action.SneakRelease);

        if (!event.isSneaking()) return;

        activateAbility(user, ActivationMethod.Sneak);

        Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class)) {
            if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("AirScooter")) {
                return;
            }
        }

        if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("FireJet")) {
            if (Game.getAbilityInstanceManager().destroyInstanceType(user, FireJet.class)) {
                return;
            }

            if (Game.getAbilityInstanceManager().destroyInstanceType(user, JetBlast.class)) {
                return;
            }

            if (Game.getAbilityInstanceManager().destroyInstanceType(user, JetBlaze.class)) {
                return;
            }
        }

        Combustion.combust(user);
        FireBurst.activateCone(user);
        AirBurst.activateCone(user);

        if (WorldUtil.getTargetEntity(user, 4) != null) {
            Game.getSequenceService().registerAction(user, Action.PunchEntity);
        } else {
            Game.getSequenceService().registerAction(user, Action.Punch);
        }

        activateAbility(user, ActivationMethod.Punch);
    }
}
