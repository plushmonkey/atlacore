package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.air.AirScooter;
import com.plushnode.atlacore.collision.AABB;
import com.plushnode.atlacore.collision.Ray;
import com.plushnode.atlacore.game.ability.fire.FireJet;
import com.plushnode.atlacore.game.ability.sequence.Action;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.platform.EntityWrapper;
import com.plushnode.atlacore.util.WorldUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.Optional;

// NOTE: test code.
public class PlayerListener implements Listener {
    private AtlaCorePlugin plugin;

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

        String abilityName = abilityDescription.getName();
        Ability ability = abilityDescription.createAbility();

        if (ability.activate(user, method)) {
            plugin.getGame().addAbility(user, ability);
            Game.info(abilityName + " created!");
        } else {
            Game.info("Failed to activate "  + abilityName);
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
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();

        User user = Game.getPlayerService().getPlayerByName(player.getName());

        activateAbility(user, ActivationMethod.Fall);

        if (user.hasElement(Game.getElementRegistry().getElementByName("Air"))) {
            event.setCancelled(true);
        }

        if (Flight.hasFlight(user)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            Game.getSequenceService().registerAction(user, Action.Interact);
        } else if(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            Game.getSequenceService().registerAction(user, Action.InteractBlock);
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

        if (WorldUtil.getTargetEntity(user, 4) != null) {
            Game.getSequenceService().registerAction(user, Action.PunchEntity);
        } else {
            Game.getSequenceService().registerAction(user, Action.Punch);
        }

        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class)) {
            if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("AirScooter")) {
                return;
            }
        }

        if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("FireJet")) {
            if (Game.getAbilityInstanceManager().destroyInstanceType(user, FireJet.class)) {
                return;
            }
        }

        activateAbility(user, ActivationMethod.Punch);
    }
}
