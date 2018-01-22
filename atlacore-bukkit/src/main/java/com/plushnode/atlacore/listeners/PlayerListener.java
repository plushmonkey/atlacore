package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.ability.air.AirScooter;
import com.plushnode.atlacore.collision.AABB;
import com.plushnode.atlacore.collision.Ray;
import com.plushnode.atlacore.util.TypeUtil;
import com.plushnode.atlacore.wrappers.EntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// NOTE: test code.
public class PlayerListener implements Listener {
    // todo: refactor this out into its own repository
    private Map<Player, User> users = new HashMap<>();
    private AtlaCorePlugin plugin;

    public PlayerListener(AtlaCorePlugin plugin) {
        this.plugin = plugin;
    }

    private User getBendingUser(Player player) {
        User user = users.get(player);

        if (user == null) {
            user = new BukkitBendingPlayer(player);

            users.put(player, user);

            System.out.println("Creating and binding user");
            // bind the abilities to slots
            AbilityDescription blaze = Game.getAbilityRegistry().getAbilityByName("Blaze");
            AbilityDescription airScooter = Game.getAbilityRegistry().getAbilityByName("AirScooter");
            AbilityDescription shockwave = Game.getAbilityRegistry().getAbilityByName("Shockwave");

            user.setSlotAbility(1, blaze);
            user.setSlotAbility(2, airScooter);
            user.setSlotAbility(3, shockwave);
        }

        return user;
    }

    private boolean activateAbility(User user, ActivationMethod method) {
        AbilityDescription abilityDescription = user.getSelectedAbility();

        if (abilityDescription == null) return false;
        if (!abilityDescription.isActivatedBy(method)) return false;
        if (user.isOnCooldown(abilityDescription)) return false;
        if (!abilityDescription.isEnabled()) return false;

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
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        User user = getBendingUser(player);

        activateAbility(user, ActivationMethod.Fall);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        User user = getBendingUser(player);

        activateAbility(user, ActivationMethod.Sneak);

        // todo: find better way to do this?
        Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
    }

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        User user = getBendingUser(player);

        activateAbility(user, ActivationMethod.Punch);

        /*Player player = event.getPlayer();
        LivingEntity target = getTargetEntity(player, 30);

        if (target == null) return;

        AbilityDescription abilityDesc = plugin.getGame().getAbilityDescription("Blaze");
        if (abilityDesc == null) {
            System.out.println("Blaze not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BukkitBendingUser user = new BukkitBendingUser(target);

        if (ability.activate(user, ActivationMethod.Sneak)) {
            plugin.getGame().addAbility(user, ability);
            System.out.println("Blaze created for target!");
        } else {
            System.out.println("Failed to activate blaze for target.");
        }*/
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
