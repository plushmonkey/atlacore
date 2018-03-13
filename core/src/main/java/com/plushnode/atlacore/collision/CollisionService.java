package com.plushnode.atlacore.collision;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.AbilityInstanceManager;
import com.plushnode.atlacore.util.Task;

import java.util.*;
import java.util.stream.Collectors;

// TODO: Some acceleration structures would be useful.
public final class CollisionService {
    private List<RegisteredCollision> collisions = new ArrayList<>();
    private Task task;

    public void start() {
        task = Game.plugin.createTaskTimer(this::run, 1, 1);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void clear() {
        collisions.clear();
    }


    private void run() {
        AbilityInstanceManager manager = Game.getAbilityInstanceManager();

        List<Ability> instances = manager.getInstances();

        if (instances.size() <= 1) return;

        Map<Ability, Collection<Collider>> colliderCache = new HashMap<>();

        for (RegisteredCollision registeredCollision : collisions) {
            List<Ability> firstAbilities = instances.stream()
                    .filter((ability) -> ability.getDescription() == registeredCollision.getFirst())
                    .collect(Collectors.toList());

            List<Ability> secondAbilities = instances.stream()
                    .filter((ability) -> ability.getDescription() == registeredCollision.getSecond())
                    .collect(Collectors.toList());

            for (Ability first : firstAbilities) {
                Collection<Collider> firstColliders = colliderCache.get(first);

                if (firstColliders == null) {
                    firstColliders = first.getColliders();
                    colliderCache.put(first, firstColliders);
                }

                if (firstColliders == null || firstColliders.isEmpty()) continue;

                for (Ability second : secondAbilities) {
                    Collection<Collider> secondColliders = colliderCache.get(second);

                    if (first.getUser().equals(second.getUser())) continue;

                    if (secondColliders == null) {
                        secondColliders = second.getColliders();
                        colliderCache.put(second, secondColliders);
                    }

                    if (secondColliders == null || secondColliders.isEmpty()) continue;

                    for (Collider firstCollider : firstColliders) {
                        for (Collider secondCollider : secondColliders) {
                            if (firstCollider.getWorld() != null && secondCollider.getWorld() != null &&
                                !firstCollider.getWorld().equals(secondCollider.getWorld()))
                            {
                                continue;
                            }

                            if (firstCollider.intersects(secondCollider)) {
                                handleCollision(first, second, firstCollider, secondCollider, registeredCollision);
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleCollision(Ability first, Ability second,
                                 Collider firstCollider, Collider secondCollider,
                                 RegisteredCollision registeredCollision)
    {
        // Collision from pov of first ability
        Collision firstCollision = new Collision(first, second,
                registeredCollision.shouldRemoveFirst(),
                registeredCollision.shouldRemoveSecond(),
                firstCollider, secondCollider);

        // Collision from pov of second ability
        Collision secondCollision = new Collision(second, first,
                registeredCollision.shouldRemoveSecond(),
                registeredCollision.shouldRemoveFirst(),
                secondCollider, firstCollider);

        first.handleCollision(firstCollision);
        second.handleCollision(secondCollision);
    }

    public void registerCollision(AbilityDescription first, AbilityDescription second, boolean removeFirst, boolean removeSecond) {
        if (first == null || second == null) return;

        collisions.add(new RegisteredCollision(first, second, removeFirst, removeSecond));
    }
}
