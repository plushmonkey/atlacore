package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.BendingPlayer;
import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.events.PlayerToggleSneakEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;

public class PlayerListener {
    private AtlaPlugin plugin;
    public PlayerListener(AtlaPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        AbilityDescription abilityDesc = plugin.getAtlaGame().getAbilityDescription("Blaze");
        if (abilityDesc == null) {
            System.out.println("Blaze not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingPlayer user = new BendingPlayer(player);

        if (ability.create(user, ActivationMethod.Sneak)) {
            plugin.getAtlaGame().addAbility(user, ability);
            System.out.println("Blaze created!");
        } else {
            System.out.println("Failed to create Blaze.");
        }
    }

    @Listener
    public void onPlayerPunch(AnimateHandEvent event) {
        Entity entity = event.getTargetEntity();

        if (!(entity instanceof Player)) return;

        AbilityDescription abilityDesc = plugin.getAtlaGame().getAbilityDescription("AirScooter");
        if (abilityDesc == null) {
            System.out.println("AirScooter not found.");
            return;
        }

        Ability ability = abilityDesc.createAbility();
        BendingPlayer user = new BendingPlayer((Player)entity);

        // todo: test code, formalize later
        for (Ability abil : Game.getAbilityInstanceManager().getPlayerInstances(user)) {
            if (abil.getClass() == ability.getClass()) {
                Game.getAbilityInstanceManager().destroyInstance(user, abil);
                return;
            }
        }

        if (ability.create(user, ActivationMethod.Punch)) {
            plugin.getAtlaGame().addAbility(user, ability);
            System.out.println("AirScooter created!");
        } else {
            System.out.println("Failed to create AirScooter.");
        }
    }
}
