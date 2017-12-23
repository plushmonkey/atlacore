package com.plushnode.atlacore;

import com.plushnode.atlacore.ability.*;
import com.plushnode.atlacore.ability.fire.Blaze;
import com.plushnode.atlacore.protection.ProtectionSystem;

import java.util.Arrays;

public class Game {
    public static CorePlugin plugin;

    private static ProtectionSystem protectionSystem;

    private AbilityRegistry abilityRegistry;
    private AbilityInstanceManager instanceManager;

    public Game(CorePlugin plugin) {
        Game.plugin = plugin;

        instanceManager = new AbilityInstanceManager();
        abilityRegistry = new AbilityRegistry();
        protectionSystem = new ProtectionSystem();

        AbilityDescription blazeDesc = new GenericAbilityDescription<>("Blaze", "Blaze it 420", 3000, Arrays.asList(ActivationMethod.Sneak), Blaze.class, false);

        System.out.println("Blaze registered.");
        abilityRegistry.registerAbility(blazeDesc);
    }

    public AbilityDescription getAbilityDescription(String abilityName) {
        return abilityRegistry.getAbilityByName(abilityName);
    }

    public void addAbility(User user, Ability instance) {
        instanceManager.addAbility(user, instance);
    }

    public void update() {
        instanceManager.update();
    }

    public static ProtectionSystem getProtectionSystem() {
        return protectionSystem;
    }

    public static void setProtectionSystem(ProtectionSystem protectionSystem) {
        Game.protectionSystem = protectionSystem;
    }
}
