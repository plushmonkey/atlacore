package com.plushnode.atlacore.game.ability.air.sequences;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.*;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Twister implements Ability {
    public static Config config = new Config();

    private User user;
    private long startTime;
    private Location base;
    private Vector3D direction;
    private Location origin;
    private List<Collider> colliders = new ArrayList<>();
    private double height;
    private Set<Entity> affected = new HashSet<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();
        this.direction = user.getDirection();

        this.base = user.getLocation().add(direction.scalarMultiply(2));
        this.base = RayCaster.cast(user, new Ray(base.add(0, 3.5, 0).toVector(), Vector3D.MINUS_J), 7.0, true, false);

        if (!isAcceptableBase()) {
            return false;
        }

        this.origin = base;
        this.height = config.height;

        return true;
    }

    @Override
    public UpdateResult update() {
        base = base.add(direction.scalarMultiply(config.speed));
        base = RayCaster.cast(user, new Ray(base.add(0, 3.5, 0).toVector(), Vector3D.MINUS_J), 7.0, true, false);

        if (!isAcceptableBase()) {
            return UpdateResult.Remove;
        }

        if (base.distanceSquared(origin) > config.range * config.range) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, base)) {
            return UpdateResult.Remove;
        }

        Location top = RayCaster.cast(user, new Ray(base.toVector(), Vector3D.PLUS_J), height, true, false);

        height = top.getY() - base.getY();
        if (height <= 0) {
            return UpdateResult.Remove;
        }

        render();

        colliders.clear();
        for (int i = 0; i < height - 1; ++i) {
            Location location = base.add(0, i, 0);
            double r = config.proximity + config.radius * (i / height);
            AABB aabb = new AABB(new Vector3D(-r, 0, -r), new Vector3D(r, 1, r)).at(location);

            colliders.add(new Disc(new OBB(aabb), new Sphere(location.toVector(), r)));
        }

        for (Collider collider : colliders) {
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    affected.add(entity);
                }
                return false;
            }, true);
        }

        for (Entity entity : affected) {
            Vector3D forceDirection = base.add(0, height, 0).subtract(entity.getLocation()).toVector().normalize();
            Vector3D force = forceDirection.scalarMultiply(config.speed);
            entity.setVelocity(force);
        }

        return UpdateResult.Continue;
    }

    private void render() {
        long time = System.currentTimeMillis();
        double cycleMS = (1000.0 * config.height / config.renderSpeed);

        for (int j = 0; j < config.streams; ++j) {
            double thetaOffset = j * ((Math.PI * 2) / config.streams);

            for (int i = 0; i < config.particlesPerStream; ++i) {
                double thisTime = time + (i / (double) config.particlesPerStream) * cycleMS;
                double f = (thisTime - startTime) / cycleMS % 1.0;

                double y = f * config.height;
                double theta = y + thetaOffset;

                double x = config.radius * f * Math.cos(theta);
                double z = config.radius * f * Math.sin(theta);

                if (y > height) {
                    continue;
                }

                Location current = base.add(x, y, z);
                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, current, 257);
            }
        }
    }

    private boolean isAcceptableBase() {
        // Remove if base couldn't be resolved to a non-solid block.
        if (base.getBlock().getBounds().contains(base.toVector())) {
            return false;
        }

        Block below = base.getBlock().getRelative(BlockFace.DOWN);
        // Remove if base was resolved to being above a non-solid block.
        if (below.getBounds().max() == null && !below.isLiquid()) {
            return false;
        }

        return true;
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
        return "Twister";
    }

    @Override
    public List<Collider> getColliders() {
        return colliders;
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
        long duration;
        double radius;
        double height;
        double range;
        double speed;
        double proximity;

        double renderSpeed;
        int streams;
        int particlesPerStream;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "sequences", "twister");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(4000);
            duration = abilityNode.getNode("duration").getLong(8000);
            radius = abilityNode.getNode("radius").getDouble(3.5);
            height = abilityNode.getNode("height").getDouble(8.0);
            range = abilityNode.getNode("range").getDouble(25.0);
            speed = abilityNode.getNode("speed").getDouble(0.35);
            proximity = abilityNode.getNode("proximity").getDouble(2.0);

            CommentedConfigurationNode renderNode = abilityNode.getNode("render");

            renderSpeed = renderNode.getNode("speed").getDouble(2.5);
            streams = renderNode.getNode("streams").getInt(6);
            particlesPerStream = renderNode.getNode("particles-per-stream").getInt(7);

            abilityNode.getNode("proximity").setComment("The amount that gets added to the radius for entity collision detection.");
        }
    }
}
