package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.SpongeBendingPlayer;
import com.plushnode.atlacore.Game;
import com.plushnode.atlacore.User;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.ability.air.AirScooter;
import com.plushnode.atlacore.ability.earth.Shockwave;
import com.plushnode.atlacore.events.PlayerToggleSneakEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.entity.Hotbar;

import java.util.HashMap;
import java.util.Map;

public class PlayerListener {
    // todo: refactor this out into its own repository
    private Map<Player, User> users = new HashMap<>();
    private AtlaPlugin plugin;

    public PlayerListener(AtlaPlugin plugin) {
        this.plugin = plugin;
    }

    private User getBendingUser(Player player) {
        User user = users.get(player);

        if (user == null) {
            user = new SpongeBendingPlayer(player);

            user.addElement(Game.getElementRegistry().getElementByName("Air"));
            user.addElement(Game.getElementRegistry().getElementByName("Fire"));
            user.addElement(Game.getElementRegistry().getElementByName("Earth"));

            users.put(player, user);

            System.out.println("Creating and binding user");
            // bind the abilities to slots
            AbilityDescription blaze = Game.getAbilityRegistry().getAbilityByName("Blaze");
            AbilityDescription airScooter = Game.getAbilityRegistry().getAbilityByName("AirScooter");
            AbilityDescription shockwave = Game.getAbilityRegistry().getAbilityByName("Shockwave");
            AbilityDescription airSwipe = Game.getAbilityRegistry().getAbilityByName("AirSwipe");
            AbilityDescription airBlast = Game.getAbilityRegistry().getAbilityByName("AirBlast");

            user.setSlotAbility(1, blaze);
            user.setSlotAbility(2, airScooter);
            user.setSlotAbility(3, shockwave);
            user.setSlotAbility(4, airSwipe);
            user.setSlotAbility(5, airBlast);
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
            plugin.getAtlaGame().addAbility(user, ability);
            System.out.println(abilityName + " created!");
        } else {
            System.out.println("Failed to activate "  + abilityName);
            return false;
        }

        return true;
    }

    @Listener
    public void onFallDamage(DamageEntityEvent event, @Root DamageSource source) {
        if (source.getType() != DamageTypes.FALL) return;

        Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        User user = getBendingUser(player);

        activateAbility(user, ActivationMethod.Fall);

        if (user.hasElement(Game.getElementRegistry().getElementByName("Air"))) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        User user = getBendingUser(player);

        activateAbility(user, ActivationMethod.Sneak);

        Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class);
    }

    @Listener
    public void onPlayerPunch(AnimateHandEvent event) {
        Entity entity = event.getTargetEntity();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        User user = getBendingUser(player);

        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class)) {
            if (user.getSelectedAbility() == Game.getAbilityRegistry().getAbilityByName("AirScooter")) {
                return;
            }
        }

        activateAbility(user, ActivationMethod.Punch);
    }
}
