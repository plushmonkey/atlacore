package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class AirShield implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private long startTime;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.startTime = System.currentTimeMillis();

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new SwappedSlotsRemovalPolicy<>(user, AirShield.class),
                new OutOfWorldRemovalPolicy(user),
                new SneakingRemovalPolicy(user, true)
        );

        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (userConfig.duration > 0 && (time > startTime + userConfig.duration)) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return UpdateResult.Remove;
        }

        Location center = getCenter();

        double height = userConfig.radius * 2;
        double spacing = height / (userConfig.renderParticleStreams + 1);

        for (int i = 1; i <= userConfig.renderParticleStreams; ++i) {
            double y = (i * spacing) - userConfig.radius;
            double factor = 1.0 - (Math.abs(y) * Math.abs(y)) / (userConfig.radius * userConfig.radius);

            // Don't render the end points that are tightly coupled.
            if (factor <= 0.2) continue;

            double theta = time * userConfig.renderParticleSpeed;

            // Offset each stream so they aren't all lined up.
            double x = userConfig.radius * factor * Math.cos(theta + i * (Math.PI * 2.0 / userConfig.renderParticleStreams));
            double z = userConfig.radius * factor * Math.sin(theta + i * (Math.PI * 2.0 / userConfig.renderParticleStreams));

            Vector3D offset = new Vector3D(x, y, z);

            Location location = center.add(offset);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.2f, 0.2f, 0.2f, 0.0f, userConfig.renderParticleCount, location, 257);
        }

        CollisionUtil.handleEntityCollisions(user, new Sphere(center.toVector(), userConfig.radius), (entity) -> {
            if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                return false;
            }

            Vector3D toEntity = entity.getLocation().subtract(center).toVector();
            Vector3D normal = VectorUtil.clearAxis(toEntity.normalize(), 1);
            if (normal.getNormSq() == 0) {
                normal = Vector3D.PLUS_I;
            }

            normal.normalize();

            Vector3D velocity = entity.getVelocity();

            double dist = toEntity.getNorm();
            double strength = ((userConfig.radius - dist) / userConfig.radius) * userConfig.maxPush;
            strength = Math.max(0.0, Math.min(1.0, strength));

            velocity = velocity.add(normal.scalarMultiply(strength));
            entity.setVelocity(velocity);

            return false;
        }, false);

        return UpdateResult.Continue;
    }

    private Location getCenter() {
        return user.getLocation().add(0, 1.8 / 2, 0);
    }

    @Override
    public void destroy() {
        user.setCooldown(this, userConfig.cooldown);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirShield";
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(new Sphere(getCenter().toVector(), userConfig.radius, user.getWorld()));
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
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        public double radius;
        @Attribute(Attributes.STRENGTH)
        public double maxPush;

        public double renderParticleSpeed;
        public int renderParticleStreams;
        public int renderParticleCount;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airshield");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(4000);
            duration = abilityNode.getNode("duration").getLong(10000);
            radius = abilityNode.getNode("radius").getDouble(4.0);
            maxPush = abilityNode.getNode("max-push").getDouble(3.0);

            CommentedConfigurationNode renderNode = abilityNode.getNode("render");
            renderParticleStreams = renderNode.getNode("particle-streams").getInt(7);
            renderParticleSpeed = renderNode.getNode("particle-speed").getDouble(1.0);
            renderParticleCount = renderNode.getNode("particle-count").getInt(5);
        }
    }
}
