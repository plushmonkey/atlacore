package com.plushnode.atlacore.game.ability.common;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public abstract class ParticleStream {
    private User user;
    private World world;
    protected Location origin;
    protected Location location;
    protected Vector3D direction;
    protected double range;
    protected double speed;
    protected double entityCollisionRadius;
    protected double abilityCollisionRadius;
    protected double damage;
    protected Collider collider;

    public ParticleStream(User user, Location origin, Vector3D direction,
                          double range, double speed, double entityCollisionRadius, double abilityCollisionRadius,
                          double damage)
    {
        this.user = user;
        this.world = user.getWorld();
        this.origin = origin;
        this.location = origin;
        this.direction = direction;
        this.range = range;
        this.speed = speed;
        this.entityCollisionRadius = entityCollisionRadius;
        this.abilityCollisionRadius = abilityCollisionRadius;
        this.damage = damage;

        this.collider = new Sphere(location.toVector(), abilityCollisionRadius, world);
    }

    // Return false to destroy this stream
    public boolean update() {
        Location previous = location;
        location = location.add(direction.scalarMultiply(speed));

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return false;
        }

        render();

        this.collider = new Sphere(location.toVector(), abilityCollisionRadius, world);

        Sphere entityCollider = new Sphere(location.toVector(), entityCollisionRadius);
        boolean hit = CollisionUtil.handleEntityCollisions(user, entityCollider, this::onEntityHit, true);

        if (hit || location.distanceSquared(origin) > range * range) {
            return false;
        }

        if (previous.equals(origin)) return true;

        Sphere blockCollider = new Sphere(location.toVector(), 0.01);
        return !CollisionUtil.handleBlockCollisions(blockCollider, previous, location, true).getFirst();
    }

    public abstract void render();

    public boolean onEntityHit(Entity entity) {
        if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
            return false;
        }

        ((LivingEntity) entity).damage(damage, user);
        return true;
    }

    public Location getLocation() {
        return location;
    }

    public Location getOrigin() {
        return origin;
    }

    public Collider getCollider() {
        return collider;
    }

    public World getWorld() {
        return world;
    }
}
