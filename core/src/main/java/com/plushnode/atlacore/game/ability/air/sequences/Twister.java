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
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Twister implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private World world;
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
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.world = user.getWorld();
        this.startTime = System.currentTimeMillis();
        this.direction = user.getDirection();
        this.direction = VectorUtil.normalizeOrElse(VectorUtil.clearAxis(direction, 1), Vector3D.PLUS_I);

        this.base = user.getLocation().add(direction.scalarMultiply(2));
        this.base = RayCaster.cast(user, new Ray(base.add(0, 3.5, 0).toVector(), Vector3D.MINUS_J), 7.0, true, false);

        if (!isAcceptableBase()) {
            return false;
        }

        this.origin = base;
        this.height = userConfig.height;

        user.setCooldown(this, userConfig.cooldown);

        return true;
    }

    @Override
    public UpdateResult update() {
        base = base.add(direction.scalarMultiply(userConfig.speed));
        base = RayCaster.cast(user, new Ray(base.add(0, 3.5, 0).toVector(), Vector3D.MINUS_J), 7.0, true, false);

        if (!isAcceptableBase()) {
            return UpdateResult.Remove;
        }

        if (base.distanceSquared(origin) > userConfig.range * userConfig.range) {
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
            double r = userConfig.proximity + userConfig.radius * (i / userConfig.height);
            AABB aabb = new AABB(new Vector3D(-r, 0, -r), new Vector3D(r, 1, r)).at(location);

            colliders.add(new Disc(new OBB(aabb, world), new Sphere(location.toVector(), r, world)));
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
            Vector3D force = forceDirection.scalarMultiply(userConfig.speed);
            entity.setVelocity(force);
        }

        return UpdateResult.Continue;
    }

    private void render() {
        long time = System.currentTimeMillis();
        double cycleMS = (1000.0 * userConfig.height / userConfig.renderSpeed);

        for (int j = 0; j < userConfig.streams; ++j) {
            double thetaOffset = j * ((Math.PI * 2) / userConfig.streams);

            for (int i = 0; i < userConfig.particlesPerStream; ++i) {
                double thisTime = time + (i / (double) userConfig.particlesPerStream) * cycleMS;
                double f = (thisTime - startTime) / cycleMS % 1.0;

                double y = f * userConfig.height;
                double theta = y + thetaOffset;

                double x = userConfig.radius * f * Math.cos(theta);
                double z = userConfig.radius * f * Math.sin(theta);

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

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.HEIGHT)
        public double height;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.SPEED)
        public double speed;
        public double proximity;

        public double renderSpeed;
        public int streams;
        public int particlesPerStream;

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
