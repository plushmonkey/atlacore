package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaCorePlugin;
import com.plushnode.atlacore.BendingPlayer;
import com.plushnode.atlacore.BendingUser;
import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.ability.earth.Shockwave;
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

import java.util.Optional;

// NOTE: test code.
public class PlayerListener implements Listener {
    private AtlaCorePlugin plugin;

    public PlayerListener(AtlaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        if (player.isSneaking()) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 1) return;

        BendingUser user = new BendingPlayer(player);

        if (!Shockwave.canFallActivate(user)) return;

        String abilityName = "Shockwave";

        AbilityDescription abilityDesc = plugin.getGame().getAbilityDescription(abilityName);
        if (abilityDesc == null) {
            System.out.println(abilityName + " not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();

        if (ability.create(user, ActivationMethod.Fall)) {
            plugin.getGame().addAbility(user, ability);
            System.out.println(abilityName + " created!");

            Shockwave shockwave = (Shockwave)ability;
            shockwave.skipCharging();
        } else {
            System.out.println("Failed to create "  + abilityName);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        int slot = player.getInventory().getHeldItemSlot();
        String abilityName = "Blaze";
        if (slot == 1) {
            abilityName = "Shockwave";
        }

        AbilityDescription abilityDesc = plugin.getGame().getAbilityDescription(abilityName);
        if (abilityDesc == null) {
            System.out.println(abilityName + " not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingUser user = new BendingPlayer(player);

        if (ability.create(user, ActivationMethod.Sneak)) {
            plugin.getGame().addAbility(user, ability);
            System.out.println(abilityName + " created!");
        } else {
            System.out.println("Failed to create "  + abilityName);
        }
    }

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();

        String abilityName = "AirScooter";

        int slot = player.getInventory().getHeldItemSlot();
        if (slot == 1) {
            abilityName = "Shockwave";
        }

        AbilityDescription abilityDesc = plugin.getGame().getAbilityDescription(abilityName);
        if (abilityDesc == null) {
            System.out.println(abilityName + " not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingUser user = new BendingPlayer(player);

        if (abilityName.equals("AirScooter")) {
            // todo: test code, formalize later
            for (Ability abil : Game.getAbilityInstanceManager().getPlayerInstances(user)) {
                if (abil.getClass() == ability.getClass()) {
                    Game.getAbilityInstanceManager().destroyInstance(user, abil);
                    return;
                }
            }

            if (ability.create(user, ActivationMethod.Punch)) {
                plugin.getGame().addAbility(user, ability);
                System.out.println(abilityName + " created!");
            } else {
                System.out.println("Failed to create " + abilityName);
            }
        } else if (abilityName.equals("Shockwave")) {
            for (Ability abil : Game.getAbilityInstanceManager().getPlayerInstances(user)) {
                if (abil.getClass() == Shockwave.class) {
                    Shockwave shockwave = (Shockwave)abil;
                    if (!shockwave.isReleased()) {
                        shockwave.activateConal();
                        break;
                    }
                }
            }
        }

        /*Player player = event.getPlayer();
        LivingEntity target = getTargetEntity(player, 30);

        if (target == null) return;

        AbilityDescription abilityDesc = plugin.getGame().getAbilityDescription("Blaze");
        if (abilityDesc == null) {
            System.out.println("Blaze not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingUser user = new BendingUser(target);

        if (ability.create(user, ActivationMethod.Sneak)) {
            plugin.getGame().addAbility(user, ability);
            System.out.println("Blaze created for target!");
        } else {
            System.out.println("Failed to create blaze for target.");
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
