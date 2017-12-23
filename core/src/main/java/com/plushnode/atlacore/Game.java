package com.plushnode.atlacore;

import com.plushnode.atlacore.ability.*;
import com.plushnode.atlacore.ability.air.AirScooter;
import com.plushnode.atlacore.ability.fire.Blaze;
import com.plushnode.atlacore.collision.CollisionSystem;
import com.plushnode.atlacore.element.Element;
import com.plushnode.atlacore.protection.ProtectionSystem;

import java.util.Arrays;
import java.util.List;

public class Game {
    public static CorePlugin plugin;

    private static ProtectionSystem protectionSystem;
    private static CollisionSystem collisionSystem;

    private static AbilityRegistry abilityRegistry;
    private static AbilityInstanceManager instanceManager;

    public Game(CorePlugin plugin, CollisionSystem collisionSystem) {
        Game.plugin = plugin;
        Game.collisionSystem = collisionSystem;

        instanceManager = new AbilityInstanceManager();
        abilityRegistry = new AbilityRegistry();
        protectionSystem = new ProtectionSystem();

        Element airElement = new Element() {
            @Override
            public String getName() {
                return "Air";
            }

            @Override
            public String getPermission() {
                return "";
            }

            @Override
            public AbilityRegistry getAbilityRegistry() {
                return new AbilityRegistry();
            }

            @Override
            public List<AbilityDescription> getPassives() {
                return Arrays.asList();
            }

            @Override
            public void addPassive(AbilityDescription passive) {

            }
        };

        Element fireElement = new Element() {
            @Override
            public String getName() {
                return "Fire";
            }

            @Override
            public String getPermission() {
                return "";
            }

            @Override
            public AbilityRegistry getAbilityRegistry() {
                return new AbilityRegistry();
            }

            @Override
            public List<AbilityDescription> getPassives() {
                return Arrays.asList();
            }

            @Override
            public void addPassive(AbilityDescription passive) {

            }
        };

        AbilityDescription blazeDesc = new GenericAbilityDescription<>("Blaze", "Blaze it 420", fireElement, 3000, Arrays.asList(ActivationMethod.Sneak), Blaze.class, false);
        AbilityDescription scooterDesc = new GenericAbilityDescription<>("AirScooter", "scoot scoot", airElement, 3000, Arrays.asList(ActivationMethod.Punch), AirScooter.class, true);

        System.out.println("Blaze registered.");
        abilityRegistry.registerAbility(blazeDesc);
        abilityRegistry.registerAbility(scooterDesc);
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

    public static CollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    public static AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    public static AbilityInstanceManager getAbilityInstanceManager() {
        return instanceManager;
    }
}
