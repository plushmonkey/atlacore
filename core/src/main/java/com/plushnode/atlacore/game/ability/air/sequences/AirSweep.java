package com.plushnode.atlacore.game.ability.air.sequences;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.ParticleStream;
import com.plushnode.atlacore.math.CubicHermiteSpline;
import com.plushnode.atlacore.platform.*;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AirSweep implements Ability {
    public static Config config = new Config();

    private User user;
    private long startTime;
    private CubicHermiteSpline spline;
    private List<ParticleStream> streams;
    private List<Entity> affected = new ArrayList<>();
    private int launchCount;
    private boolean linear;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();
        this.spline = new CubicHermiteSpline(0.1);
        this.streams = new ArrayList<>();
        this.linear = "linear".equalsIgnoreCase(config.interpolationMethod);

        user.setCooldown(this);

        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (time < startTime + config.sampleTime) {
            // Sample target positions and add them to the spline.
            // These positions get interpolated later.
            Vector3D position = user.getEyeLocation().add(user.getDirection().scalarMultiply(config.range)).toVector();
            spline.addKnot(position);

            return UpdateResult.Continue;
        }

        // Clear out any intermediate knots so it becomes a line.
        if (linear && spline.getKnots().size() > 2) {
            List<Vector3D> knots = spline.getKnots();
            Vector3D begin = knots.get(0);
            Vector3D end = knots.get(knots.size() - 1);

            knots.clear();
            spline.addKnot(begin);
            spline.addKnot(end);
        }

        // Launch a few streams per tick to give it a delay.
        if (launchCount < config.streamCount) {
            // Only launch enough to hit stream count.
            int count = Math.min(config.streamCount - launchCount, config.streamCount / 10);

            for (int i = 0; i < count; ++i) {
                // Interpolate based on the initial samples gathered.
                Vector3D target = spline.interpolate(launchCount / (double)config.streamCount);
                Vector3D direction = target.subtract(user.getEyeLocation().toVector()).normalize();

                streams.add(new SweepStream(user, user.getEyeLocation(), direction, config.range, config.speed, 0.5, 0.5, config.damage));
                ++launchCount;
            }
        }

        for (Iterator<ParticleStream> iterator = streams.iterator(); iterator.hasNext();) {
            ParticleStream stream = iterator.next();

            if (!stream.update()) {
                iterator.remove();
            }
        }

        return (streams.isEmpty() && launchCount >= config.streamCount) ? UpdateResult.Remove : UpdateResult.Continue;
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
        return "AirSweep";
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    @Override
    public Collection<Collider> getColliders() {
        return streams.stream()
                .map(ParticleStream::getCollider)
                .collect(Collectors.toList());
    }

    private class SweepStream extends ParticleStream {
        public SweepStream(User user, Location origin, Vector3D direction, double range, double speed, double entityCollisionRadius, double abilityCollisionRadius, double damage) {
            super(user, origin, direction, range, speed, entityCollisionRadius, abilityCollisionRadius, damage);
        }

        @Override
        public void render() {
            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
        }

        @Override
        public boolean onEntityHit(Entity entity) {
            if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                return false;
            }

            if (affected.contains(entity)) {
                return true;
            }

            ((LivingEntity) entity).damage(damage, user);
            entity.setVelocity(direction.scalarMultiply(config.knockback));

            affected.add(entity);

            return true;
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double range;
        public double speed;
        public double damage;
        public double knockback;
        public int sampleTime;
        public int streamCount;
        public String interpolationMethod;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "sequences", "airsweep");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            range = abilityNode.getNode("range").getDouble(14.0);
            speed = abilityNode.getNode("speed").getDouble(1.4);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            knockback = abilityNode.getNode("knockback").getDouble(3.5);
            sampleTime = abilityNode.getNode("sample-time").getInt(400);
            streamCount = abilityNode.getNode("stream-count").getInt(30);
            interpolationMethod = abilityNode.getNode("interpolation-method").getString("spline");

            abilityNode.getNode("interpolation-method").setComment("This sets the method in which the sweep interpolates the samples.\nThis can be set to either 'linear' or 'spline'.");
        }
    }
}
