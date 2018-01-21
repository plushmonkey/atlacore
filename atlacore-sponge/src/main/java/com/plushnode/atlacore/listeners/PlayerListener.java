package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.BendingPlayer;
import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.ability.earth.Shockwave;
import com.plushnode.atlacore.events.PlayerToggleSneakEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.entity.Hotbar;

public class PlayerListener {
    private AtlaPlugin plugin;
    public PlayerListener(AtlaPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onFallDamage(DamageEntityEvent event, @Root DamageSource source) {
        if (source.getType() != DamageTypes.FALL) return;
        Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        Hotbar hotbar = player.getInventory().query(Hotbar.class);
        int slot = hotbar.getSelectedSlotIndex();
        if (slot != 1) return;

        BendingPlayer user = new BendingPlayer(player);

        if (!Shockwave.canFallActivate(user));

        String abilityName = "Shockwave";

        AbilityDescription abilityDesc = plugin.getAtlaGame().getAbilityDescription(abilityName);
        if (abilityDesc == null) {
            System.out.println(abilityName + " not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();

        if (ability.create(user, ActivationMethod.Fall)) {
            plugin.getAtlaGame().addAbility(user, ability);
            System.out.println(abilityName + " created!");

            Shockwave shockwave = (Shockwave)ability;
            shockwave.skipCharging();
        } else {
            System.out.println("Failed to create "  + abilityName);
        }
    }

    @Listener
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        Hotbar hotbar = player.getInventory().query(Hotbar.class);
        int slot = hotbar.getSelectedSlotIndex();
        String abilityName = "Blaze";
        if (slot == 1) {
            abilityName = "Shockwave";
        }

        AbilityDescription abilityDesc = plugin.getAtlaGame().getAbilityDescription(abilityName);
        if (abilityDesc == null) {
            System.out.println(abilityName + " not found.");
            return;
        }


        Ability ability = abilityDesc.createAbility();
        BendingPlayer user = new BendingPlayer(player);

        if (ability.create(user, ActivationMethod.Sneak)) {
            plugin.getAtlaGame().addAbility(user, ability);
            System.out.println(abilityName + " created!");
        } else {
            System.out.println("Failed to create "  + abilityName);
        }
    }

    @Listener
    public void onPlayerPunch(AnimateHandEvent event) {
        Entity entity = event.getTargetEntity();

        if (!(entity instanceof Player)) return;

        Hotbar hotbar = ((Player)entity).getInventory().query(Hotbar.class);
        int slot = hotbar.getSelectedSlotIndex();
        String abilityName = "AirScooter";
        if (slot == 1) {
            abilityName = "Shockwave";
        }

        AbilityDescription abilityDesc = plugin.getAtlaGame().getAbilityDescription(abilityName);
        if (abilityDesc == null) {
            System.out.println(abilityName + " not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingPlayer user = new BendingPlayer((Player)entity);

        if (abilityName.equals("AirScooter")) {
            // todo: test code, formalize later
            for (Ability abil : Game.getAbilityInstanceManager().getPlayerInstances(user)) {
                if (abil.getClass() == ability.getClass()) {
                    Game.getAbilityInstanceManager().destroyInstance(user, abil);
                    return;
                }
            }

            if (ability.create(user, ActivationMethod.Punch)) {
                plugin.getAtlaGame().addAbility(user, ability);
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
    }
}
