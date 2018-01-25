package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.collision.Ray;
import com.plushnode.atlacore.collision.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsDeadRemovalPolicy;
import com.plushnode.atlacore.policies.removal.OutOfRangeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.SwappedSlotsRemovalPolicy;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AirBlast implements Ability {
    public static Config config = new Config();
    private User user;
    private Location origin;
    private Location location;
    private Vector3D direction;
    private boolean launched;
    private boolean selectedOrigin;

    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        for (AirBlast blast : Game.getAbilityInstanceManager().getPlayerInstances(user, AirBlast.class)) {
            if (!blast.launched) {
                if (method == ActivationMethod.Sneak) {
                    blast.selectOrigin();

                    if (!Game.getProtectionSystem().canBuild(user, blast.origin)) {
                        Game.getAbilityInstanceManager().destroyInstance(user, blast);
                    }
                } else {
                    blast.launch();
                }

                return false;
            }
        }

        this.user = user;
        this.launched = false;
        this.selectedOrigin = false;

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, config.selectOutOfRange, () -> origin),
                new SwappedSlotsRemovalPolicy<>(user, AirBlast.class)
        );

        if (method == ActivationMethod.Sneak) {
            selectOrigin();
        } else {
            this.origin = user.getEyeLocation();
            launch();
        }

        return true;
    }

    private void selectOrigin() {
        Ray ray = new Ray(user.getEyeLocation(), user.getDirection());

        this.origin = Game.getCollisionSystem().castRay(user.getWorld(), ray, config.selectRange);

        this.selectedOrigin = true;
    }

    private void launch() {
        this.launched = true;
        this.location = origin;
        this.direction = user.getDirection();

        removalPolicy.removePolicyType(IsDeadRemovalPolicy.class);
        removalPolicy.removePolicyType(OutOfRangeRemovalPolicy.class);
        removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);

        user.setCooldown(this);
    }

    @Override
    public boolean update() {
        if (removalPolicy.shouldRemove()) {
            return true;
        }

        if (!launched) {
            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL,
                    (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random(),
                    config.selectParticles, origin, 257);
        } else {
            location = location.add(direction.scalarMultiply(config.speed));

            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL,
                    0.275f, 0.275f, 0.275f, 0.0f,
                    config.particles, location, 257);

            if (location.distanceSquared(origin) >= config.range * config.range) {
                return true;
            }

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return true;
            }

            Sphere collider = new Sphere(location.toVector(), config.entityCollisionRadius);

            // Handle user separately from the general entity collision.
            if (this.selectedOrigin) {
                if (Game.getCollisionSystem().getAABB(user).at(user.getLocation()).intersects(collider)) {
                    affect(user);
                    return true;
                }
            }

            boolean hit = Game.getCollisionSystem().handleEntityCollisions(user, collider, this::affect, false, false);

            if (hit) {
                return true;
            }
        }

        return false;
    }

    private boolean affect(Entity entity) {
        double factor = config.otherPush;

        if (entity.equals(user)) {
            factor = config.selfPush;
        }

        factor *= 1.0 - (location.distance(origin) / (2 * config.range));

        entity.setVelocity(direction.scalarMultiply(factor));

        return entity instanceof LivingEntity;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getName() {
        return "AirBlast";
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        double range;
        double speed;
        double entityCollisionRadius;
        int particles;
        double selfPush;
        double otherPush;

        double selectRange;
        double selectOutOfRange;
        int selectParticles;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airblast");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            range = abilityNode.getNode("range").getDouble(25.0);
            speed = abilityNode.getNode("speed").getDouble(1.25);
            particles = abilityNode.getNode("particles").getInt(6);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(2.0);

            selfPush = abilityNode.getNode("push").getNode("self").getDouble(2.5);
            otherPush = abilityNode.getNode("push").getNode("other").getDouble(3.5);

            selectRange = abilityNode.getNode("select").getNode("range").getDouble(10.0);
            selectOutOfRange = abilityNode.getNode("select").getNode("out-of-range").getDouble(35.0);
            selectParticles = abilityNode.getNode("select").getNode("particles").getInt(4);

            abilityNode.getNode("speed").setComment("How many meters the blast advances with each tick.");
        }
    }
}
