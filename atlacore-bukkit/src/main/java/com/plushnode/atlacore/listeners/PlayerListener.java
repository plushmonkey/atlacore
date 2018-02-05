package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.board.BendingBoard;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.game.ability.fire.Combustion;
import com.plushnode.atlacore.game.ability.fire.FireBurst;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.ability.fire.HeatControl;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlast;
import com.plushnode.atlacore.game.ability.fire.sequences.JetBlaze;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.game.element.Elements;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.Task;
import com.plushnode.atlacore.util.WorldUtil;
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
    private AtlaCorePlugin plugin;
    // TODO: Move into appropriate place
    private Map<String, BendingBoard> boards = new HashMap<>();
    private Task boardTask = null;

    public PlayerListener(AtlaCorePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean activateAbility(User user, ActivationMethod method) {
        AbilityDescription abilityDescription = user.getSelectedAbility();

        if (abilityDescription == null) return false;
        if (!abilityDescription.isActivatedBy(method)) return false;
        if (user.isOnCooldown(abilityDescription)) return false;
        if (!abilityDescription.isEnabled()) return false;
        if (!user.hasElement(abilityDescription.getElement())) return false;

        Ability ability = abilityDescription.createAbility();

        if (ability.activate(user, method)) {
            plugin.getGame().addAbility(user, ability);
        } else {
            return false;
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Create the player on the next tick so createPlayer can find the Bukkit player.
        plugin.createTask(() -> {
            Game.getPlayerService().createPlayer(player.getUniqueId(), player.getName(), (p) -> {
                if (p == null) {
                    // This can happen if the player logs off before the player is created.
                    return;
                }

                boards.put(p.getName(), new BendingBoard(p));

                if (boardTask == null) {
                    boardTask = plugin.createTaskTimer(() -> {
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

        if (user.hasElement(Elements.AIR)) {
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
        FireBurst.activateConalBurst(user);

        if (WorldUtil.getTargetEntity(user, 4) != null) {
            Game.getSequenceService().registerAction(user, Action.PunchEntity);
        } else {
            Game.getSequenceService().registerAction(user, Action.Punch);
        }

        activateAbility(user, ActivationMethod.Punch);
    }
}
