package com.plushnode.atlacore.game.ability.fire.sequences;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Disc;
import com.plushnode.atlacore.collision.geometry.OBB;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class FireWheel implements Ability {
    public static Config config = new Config();

    private User user;
    private Location location;
    private Location origin;
    private Vector3D direction;
    private Disc collider;
    private Disc entityCollider;

    private enum CollisionResolution {
        None,
        Resolved,
        Failure
    }

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.direction = VectorUtil.clearAxis(user.getDirection(), 1).normalize();
        this.location = this.user.getLocation().add(direction.scalarMultiply(config.speed));

        if (this.location.getBlock().isLiquid()) {
            return false;
        }

        AABB bounds = new AABB(new Vector3D(-0.1, -config.radius, -config.radius), new Vector3D(0.1, config.radius, config.radius));
        OBB obb = new OBB(bounds, new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw())));
        Sphere sphere = new Sphere(this.location.toVector(), config.radius);
        this.collider = new Disc(obb, sphere);

        CollisionResolution resolution = resolveInitialCollisions(WorldUtil.getNearbyBlocks(location, config.radius * 5), config.radius * 2);

        if (resolution == CollisionResolution.Failure) {
            return false;
        }

        this.origin = location;

        // Create a separate collider for colliding with entities.
        AABB entityBounds = new AABB(new Vector3D(-config.entityCollisionRadius, -config.radius, -config.radius), new Vector3D(config.entityCollisionRadius, config.radius, config.radius));
        OBB entityOBB = new OBB(entityBounds, new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw())));
        Sphere entitySphere = new Sphere(this.location.toVector(), Math.max(config.radius, config.entityCollisionRadius));
        this.entityCollider = new Disc(entityOBB, entitySphere);

        user.setCooldown(this);

        return true;
    }

    @Override
    public UpdateResult update() {
        location = location.add(direction.scalarMultiply(config.speed));

        if (location.distanceSquared(origin) > config.range * config.range) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        if (!resolveMovement()) {
            return UpdateResult.Remove;
        }

        if (location.subtract(0, config.radius + 1, 0).getBlock().isLiquid()) {
            return UpdateResult.Remove;
        }

        Vector3D rotationAxis = Vector3D.PLUS_J.crossProduct(direction).normalize();

        for (double angle = 0; angle < 360; ++angle) {
            Vector3D offset = VectorUtil.rotate(direction, rotationAxis, Math.toRadians(angle)).scalarMultiply(config.radius);
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.0f, 1, location.add(offset), 257);
        }

        Disc checkCollider = this.entityCollider.at(location.toVector());

        boolean hit = CollisionUtil.handleEntityCollisions(user, checkCollider, (entity) -> {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).damage(config.damage, user);
                return true;
            }

            return false;
        }, true);

        if (hit) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    // Try to resolve wheel location by checking collider-block intersections.
    private boolean resolveMovement() {
        Collection<Block> nearbyBlocks = WorldUtil.getNearbyBlocks(location, config.radius * 5);

        CollisionResolution resolution = resolveInitialCollisions(nearbyBlocks, 1.0);

        if (resolution == CollisionResolution.Failure) {
            return false;
        }

        Disc checkCollider = this.collider.at(location.toVector());

        // Try to fall if the block below doesn't have a bounding box.
        Block blockBelow = location.subtract(0, collider.getHalfExtents().getY() + 1, 0).getBlock();
        if (resolution == CollisionResolution.None && blockBelow.getBounds().getHalfExtents().getY() == 0) {
            this.location = location.subtract(0, 1, 0);
            checkCollider = this.collider.at(location.toVector());

            for (Block block : nearbyBlocks) {
                AABB blockBounds = block.getBounds().at(block.getLocation());

                if (blockBounds.intersects(checkCollider)) {
                    // Go back up if there is a collision after falling.
                    this.location = location.add(0, 1, 0);
                    checkCollider = this.collider.at(location.toVector());
                    break;
                }
            }
        }

        // Check if there's any final collisions after all movements.
        for (Block block : nearbyBlocks) {
            AABB blockBounds = block.getBounds().at(block.getLocation());

            if (blockBounds.intersects(checkCollider)) {
                return false;
            }
        }

        return true;
    }

    private CollisionResolution resolveInitialCollisions(Collection<Block> nearbyBlocks, double maxResolution) {
        Disc checkCollider = this.collider.at(location.toVector());

        for (Block block : nearbyBlocks) {
            AABB blockBounds = block.getBounds().at(block.getLocation());

            if (blockBounds.intersects(checkCollider)) {
                Vector3D pos = blockBounds.getPosition();
                double blockY = blockBounds.getHalfExtents().getY() * 2;

                if (pos.getY() > location.getY()) {
                    // There's a collision from above, so don't resolve it.
                    return CollisionResolution.Failure;
                }

                double colliderY = collider.getHalfExtents().getY();
                double resolution = (colliderY + blockY) - (location.getY() - pos.getY());

                if (resolution > maxResolution) {
                    return CollisionResolution.Failure;
                } else {
                    location = location.add(0, resolution, 0);
                    return CollisionResolution.Resolved;
                }
            }
        }

        return CollisionResolution.None;
    }

    @Override
    public void destroy() {

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "FireWheel";
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(this.entityCollider.at(location.toVector()));
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        double radius;
        double damage;
        double range;
        double speed;
        double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "sequences", "firewheel");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            radius = abilityNode.getNode("radius").getDouble(1.0);
            damage = abilityNode.getNode("damage").getDouble(4.0);
            range = abilityNode.getNode("range").getDouble(20.0);
            speed = abilityNode.getNode("speed").getDouble(0.55);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);

            abilityNode.getNode("speed").setComment("How many meters the streams advance with each tick.");
        }
    }
}
