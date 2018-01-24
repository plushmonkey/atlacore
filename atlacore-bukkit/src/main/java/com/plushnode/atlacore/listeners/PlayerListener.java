package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.ability.air.AirScooter;
import com.plushnode.atlacore.collision.AABB;
import com.plushnode.atlacore.collision.Ray;
import com.plushnode.atlacore.entity.user.User;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.EntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
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
            System.out.println(abilityName + " created!");
        } else {
            System.out.println("Failed to activate "  + abilityName);
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

                p.addElement(Game.getElementRegistry().getElementByName("Air"));
                p.addElement(Game.getElementRegistry().getElementByName("Fire"));
                p.addElement(Game.getElementRegistry().getElementByName("Earth"));
            });
        }, 1);
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        com.plushnode.atlacore.entity.user.Player player =
                Game.getPlayerService().getPlayerByName(event.getPlayer().getName());

        Game.getPlayerService().invalidatePlayer(player);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();

        User user = Game.getPlayerService().getPlayerByName(player.getName());

        activateAbility(user, ActivationMethod.Fall);

        if (user.hasElement(Game.getElementRegistry().getElementByName("Air"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        activateAbility(user, ActivationMethod.Sneak);

        Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
    }

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        User user = Game.getPlayerService().getPlayerByName(player.getName());

        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class)) {
            if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("AirScooter")) {
                return;
            }
        }

        activateAbility(user, ActivationMethod.Punch);
    }

    private LivingEntity getTargetEntity(Player player, int range) {
        Ray ray = new Ray(TypeUtil.adapt(player.getEyeLocation()), TypeUtil.adapt(player.getEyeLocation().getDirection()));

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), range, range, range)) {
            if (entity == player) continue;
            if (!(entity instanceof LivingEntity)) continue;

            EntityWrapper ew = new EntityWrapper(entity);
            AABB entityBounds = Game.getCollisionSystem().getAABB(ew).at(ew.getLocation());

            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                double dist = result.get();

                if (dist < closestDist) {
                    closest = (LivingEntity)entity;
                    closestDist = dist;
                }
            }
        }

        if (closestDist > range) {
            return null;
        }

        return closest;
    }
}
