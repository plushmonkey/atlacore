package com.plushnode.atlacore.ability;

import com.plushnode.atlacore.User;
import com.plushnode.atlacore.element.Element;

import java.util.*;

public class AbilityInstanceManager {
    private Map<User, List<Ability>> globalInstances = new HashMap<>();

    // Add a new ability instance that should be updated every tick
    public void addAbility(User user, Ability instance) {
        List<Ability> playerInstanceList = globalInstances.get(user);

        if (playerInstanceList == null) {
            playerInstanceList = new ArrayList<>();
            globalInstances.put(user, playerInstanceList);
        }

        playerInstanceList.add(instance);
        System.out.println("Active instances: " + getInstanceCount());
    }

    public void createPassives(User user) {
        List<Element> elements = user.getElements();

        for (Element element : elements) {
            List<AbilityDescription> passives = element.getPassives();
            if (passives != null) {
                for (AbilityDescription passive : passives) {
                    Ability ability = passive.createAbility();
                    if (ability.create(user, ActivationMethod.Passive)) {
                        this.addAbility(user, ability);
                    }
                }
            }
        }
    }

    // Get the number of active abilities.
    public int getInstanceCount() {
        int size = 0;
        for (List<Ability> instances : globalInstances.values()) {
            size += instances.size();
        }
        return size;
    }

    public List<Ability> getPlayerInstances(User user) {
        return globalInstances.get(user);
    }

    public List<Ability> getInstances() {
        List<Ability> totalInstances = new ArrayList<>();

        for (List<Ability> instances : globalInstances.values()) {
            totalInstances.addAll(instances);
        }

        return totalInstances;
    }

    // Destroy every instance created by a player.
    // Calls destroy on the ability before removing it.
    public void destroyPlayerInstances(User user) {
        List<Ability> instances = globalInstances.get(user);

        if (instances != null) {
            for (Ability ability : instances) {
                ability.destroy();
            }

            instances.clear();
        }
        globalInstances.remove(user);

        System.out.println("Active instances: " + getInstanceCount());
    }

    // Destroy all instances created by every player.
    // Calls destroy on the ability before removing it.
    public void destroyAllInstances() {
        Iterator<Map.Entry<User, List<Ability>>> playerIterator = globalInstances.entrySet().iterator();
        while (playerIterator.hasNext()) {
            Map.Entry<User, List<Ability>> entry = playerIterator.next();
            List<Ability> instances = entry.getValue();

            for (Ability ability : instances) {
                ability.destroy();
            }

            instances.clear();
            playerIterator.remove();
        }
    }

    public void onAbilityChange(User user, AbilityDescription oldAbility, AbilityDescription newAbility) {
        List<Ability> instances = getPlayerInstances(user);

        if (instances == null) return;
        Iterator<Ability> iterator = instances.iterator();
        while (iterator.hasNext()) {
            Ability ability = iterator.next();

            if (ability.onAbilityChange(user, oldAbility, newAbility)) {
                ability.destroy();
                iterator.remove();
                System.out.println("Active instances: " + getInstanceCount());
            }
        }
    }

    // Updates each ability every tick. Destroys the ability if ability.update() returns true.
    public void update() {
        Iterator<Map.Entry<User, List<Ability>>> playerIterator = globalInstances.entrySet().iterator();

        while (playerIterator.hasNext()) {
            Map.Entry<User, List<Ability>> entry = playerIterator.next();
            List<Ability> instances = entry.getValue();
            Iterator<Ability> iterator = instances.iterator();

            while (iterator.hasNext()) {
                Ability ability = iterator.next();

                if (ability.update()) {
                    ability.destroy();
                    iterator.remove();
                    System.out.println("Active instances: " + getInstanceCount());
                }
            }

            if (entry.getValue().isEmpty()) {
                playerIterator.remove();
            }
        }
    }
}

