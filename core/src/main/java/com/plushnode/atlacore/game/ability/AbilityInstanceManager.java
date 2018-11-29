package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.element.Element;

import java.util.*;
import java.util.stream.Collectors;

public class AbilityInstanceManager {
    private Map<User, List<Ability>> globalInstances = new HashMap<>();

    // Add a new ability instance that should be updated every tick
    public void addAbility(User user, Ability instance) {
        globalInstances.computeIfAbsent(user, key -> new ArrayList<>())
                .add(instance);
    }

    public void createPassives(User user) {
        List<Element> elements = user.getElements();

        for (Element element : elements) {
            List<AbilityDescription> passives = element.getPassives();

            if (passives != null) {
                for (AbilityDescription passive : passives) {
                    destroyInstanceType(user, passive);

                    if (!passive.isEnabled()) continue;
                    if (!user.hasPermission("atla.ability." + passive.getName())) continue;

                    Ability ability = passive.createAbility();

                    if (ability.activate(user, ActivationMethod.Passive)) {
                        this.addAbility(user, ability);
                    }
                }
            }
        }
    }

    public void clearPassives(User user) {
        List<Ability> abilities = new ArrayList<>(getPlayerInstances(user));

        for (Ability instance : abilities) {
            if (instance.getDescription().isActivatedBy(ActivationMethod.Passive)) {
                this.destroyInstance(user, instance);
            }
        }
    }

    public boolean hasAbility(User user, AbilityDescription desc) {
        Ability checkAbility = desc.createAbility();

        List<Ability> abilities = globalInstances.get(user);
        if (abilities == null) return false;

        for (Ability ability : abilities) {
            if (ability.getClass().equals(checkAbility.getClass())) {
                return true;
            }
        }

        return false;
    }

    public void destroyInstance(User user, Ability ability) {
        List<Ability> abilities = globalInstances.get(user);
        if (ability == null) {
            return;
        }

        abilities.remove(ability);
        ability.destroy();
    }

    public boolean destroyInstanceType(User user, AbilityDescription abilityDesc) {
        if (abilityDesc == null) return false;

        Ability destroyAbility = abilityDesc.createAbility();
        return destroyInstanceType(user, destroyAbility.getClass());
    }

    public boolean destroyInstanceType(User user, Class<? extends Ability> clazz) {
        List<Ability> abilities = globalInstances.get(user);
        if (abilities == null) return false;

        boolean destroyed = false;
        for (Iterator<Ability> iterator = abilities.iterator(); iterator.hasNext();) {
            Ability ability = iterator.next();

            if (ability.getClass() == clazz) {
                iterator.remove();
                ability.destroy();
                destroyed = true;
            }
        }

        return destroyed;
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
        List<Ability> abilities = globalInstances.get(user);
        if (abilities == null) return new ArrayList<>();
        return abilities;
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> List<T> getPlayerInstances(User user, Class<T> type) {
        List<Ability> abilities = globalInstances.get(user);
        if (abilities == null) return new ArrayList<>();

        return abilities.stream().filter(a -> a.getClass() == type).map(a -> (T)a).collect(Collectors.toList());
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

    // Updates each ability every tick. Destroys the ability if ability.update() returns UpdateResult.Remove.
    public void update() {
        Iterator<Map.Entry<User, List<Ability>>> playerIterator = globalInstances.entrySet().iterator();

        // Store the removed abilities here so any abilities added during Ability#destroy won't be concurrent.
        List<Ability> removed = new ArrayList<>();

        while (playerIterator.hasNext()) {
            Map.Entry<User, List<Ability>> entry = playerIterator.next();
            List<Ability> instances = entry.getValue();
            Iterator<Ability> iterator = instances.iterator();

            while (iterator.hasNext()) {
                Ability ability = iterator.next();

                UpdateResult result = UpdateResult.Remove;

                try {
                    result = ability.update();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (result == UpdateResult.Remove) {
                    removed.add(ability);
                    iterator.remove();
                }
            }

            if (entry.getValue().isEmpty()) {
                playerIterator.remove();
            }
        }

        removed.forEach(Ability::destroy);
    }
}
