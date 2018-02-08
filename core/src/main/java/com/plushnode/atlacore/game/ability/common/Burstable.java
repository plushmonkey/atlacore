package com.plushnode.atlacore.game.ability.common;

import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Burstable extends Ability {
    void initialize(User user, Location location, Vector3D direction);
    void setRenderInterval(long interval);
    void setRenderParticleCount(int count);
}
