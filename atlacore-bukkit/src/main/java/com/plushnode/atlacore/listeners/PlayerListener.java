package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaCorePlugin;
import com.plushnode.atlacore.BendingPlayer;
import com.plushnode.atlacore.BendingUser;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.collision.AABB;
import com.plushnode.atlacore.collision.Ray;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        AbilityDescription abilityDesc = plugin.getGame().getAbilityDescription("Blaze");
        if (abilityDesc == null) {
            System.out.println("Blaze not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingUser user = new BendingPlayer(player);

        if (ability.create(user, ActivationMethod.Sneak)) {
            plugin.getGame().addAbility(user, ability);
            System.out.println("Blaze created!");
        } else {
            System.out.println("Failed to create blaze.");
        }
    }

    // Test code for activating abilities on general entities.
    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
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
        }
    }

    private LivingEntity getTargetEntity(Player player, int range) {
        Ray ray = new Ray(player.getEyeLocation(), player.getEyeLocation().getDirection());

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), range, range, range)) {
            if (entity == player) continue;
            if (!(entity instanceof LivingEntity)) continue;

            AABB entityBounds = new AABB(entity).at(entity.getLocation());

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
