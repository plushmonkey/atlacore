package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.game.element.Element;

import java.util.*;
import java.util.stream.Collectors;

public class AbilityInstanceManager {
    private Map<User, List<Ability>> globalInstances = new HashMap<>();
    private List<UserInstance> addQueue = new ArrayList<>();

    private static class UserInstance {
        User user;
        Ability instance;

        UserInstance(User user, Ability instance) {
            this.user = user;
            this.instance = instance;
        }
    }

    // Add a new ability instance that should be updated every tick
    // This is deferred until next update to prevent concurrent modifications.
    public void addAbility(User user, Ability instance) {
        if (instance instanceof MultiAbility) {
            user.pushSlotContainer(((MultiAbility) instance).getSlots());

            if (user instanceof Player) {
                ((Player) user).setHeldItemSlot(0);
            }
        }

        addQueue.add(new UserInstance(user, instance));
    }

    public void changeOwner(Ability ability, User user) {
        if (ability.getUser().equals(user)) return;

        List<Ability> previousUserInstances = globalInstances.get(ability.getUser());
        if (previousUserInstances != null) {
            previousUserInstances.remove(ability);
        }

        globalInstances.computeIfAbsent(user, k -> new ArrayList<>()).add(ability);

        ability.setUser(user);
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

    public boolean hasAbility(User user, Class<? extends Ability> abilityType) {
        List<Ability> abilities = globalInstances.get(user);
        if (abilities == null) return false;

        for (Ability ability : abilities) {
            if (ability.getClass().equals(abilityType)) {
                return true;
            }
        }

        return false;
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
        destroyAbility(ability);
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
                destroyAbility(ability);
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

    public <T extends Ability> T getFirstInstance(User user, Class<T> type) {
        List<Ability> abilities = globalInstances.get(user);

        if (abilities == null) return null;

        for (Ability ability : abilities) {
            if (ability.getClass() == type) {
                return type.cast(ability);
            }
        }

        return null;
    }

    public List<Ability> getInstances() {
        List<Ability> totalInstances = new ArrayList<>();

        for (List<Ability> instances : globalInstances.values()) {
            totalInstances.addAll(instances);
        }

        return totalInstances;
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> List<T> getInstances(Class<T> type) {
        List<T> totalInstances = new ArrayList<>();

        for (List<Ability> instances : globalInstances.values()) {
            for (Ability ability : instances) {
                if (ability.getClass().equals(type)) {
                    totalInstances.add((T)ability);
                }
            }
        }

        return totalInstances;
    }

    // Destroy every instance created by a player.
    // Calls destroy on the ability before removing it.
    public void destroyPlayerInstances(User user) {
        List<Ability> instances = globalInstances.get(user);

        if (instances != null) {
            for (Ability ability : instances) {
                destroyAbility(ability);
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
                destroyAbility(ability);
            }

            instances.clear();
            playerIterator.remove();
        }
    }

    // Updates each ability every tick. Destroys the ability if ability.update() returns UpdateResult.Remove.
    public void update() {
        for (UserInstance userInstance : addQueue) {
            globalInstances.computeIfAbsent(userInstance.user, key -> new ArrayList<>())
                    .add(userInstance.instance);
        }
        addQueue.clear();

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

        removed.forEach(this::destroyAbility);
    }

    // Calls ability destroy and cleans up slot containers.
    private void destroyAbility(Ability ability) {
        ability.destroy();

        if (ability instanceof MultiAbility) {
            ability.getUser().popSlotContainer();
        }
    }
}
