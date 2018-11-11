package com.plushnode.atlacore.game.ability;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.User;

import java.util.Collection;
import java.util.Collections;

public interface Ability {
    // return true if the ability was activated
    boolean activate(User user, ActivationMethod method);
    // Return UpdateResult.Remove if the ability should be removed.
    UpdateResult update();

    void destroy();

    User getUser();
    String getName();

    default AbilityDescription getDescription() {
        return Game.getAbilityRegistry().getAbilityDescription(this);
    }

    default Collection<Collider> getColliders() {
        return Collections.emptyList();
    }

    void handleCollision(Collision collision);
    void recalculateConfig();
}
