package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class FireBurst implements Ability {
    public static Config config = new Config();
    private static final double BUFFER_DISTANCE = 3;

    private User user;
    private long startTime;
    private boolean released;
    private double distance;
    private Location origin;
    private boolean conal;
    private Vector3D userDirection;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();
        this.released = false;
        this.distance = BUFFER_DISTANCE;
        this.conal = false;

        return true;
    }

    public static void activateConalBurst(User user) {
        for (FireBurst burst : Game.getAbilityInstanceManager().getPlayerInstances(user, FireBurst.class)) {
            if (burst.isCharged() && !burst.released) {
                burst.conal = true;
                burst.userDirection = user.getDirection();
                burst.release();
                return;
            }
        }
    }

    @Override
    public UpdateResult update() {
        boolean charged = this.isCharged();

        if (!charged && !user.isSneaking()) {
            return UpdateResult.Remove;
        }

        if (charged && !this.released) {
            if (this.user.isSneaking()) {
                // Display particles showing that it's ready to fire.
                Vector3D direction = user.getDirection();
                Location displayLocation = user.getEyeLocation().add(direction);

                Vector3D side = direction.crossProduct(Vector3D.PLUS_J).normalize();
                displayLocation = displayLocation.add(side.scalarMultiply(0.5));

                Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.25f, 0.25f, 0.25f, 0.0f, 3, displayLocation, 257);
                return UpdateResult.Continue;
            } else {
                release();
            }
        }

        if (this.released) {
            if (distance > config.range + BUFFER_DISTANCE) {
                return UpdateResult.Remove;
            }

            double remainder = config.speed - Math.floor(config.speed);
            int steps = (int)Math.ceil(config.speed);
            for (int i = 0; i < steps; ++i) {
                double stepSpeed = 1.0;
                if (remainder > 0 && i == steps - 1) {
                    stepSpeed = remainder;
                }

                distance += stepSpeed;
                render();
            }
        }

        return UpdateResult.Continue;
    }

    private void release() {
        user.setCooldown(this);

        this.released = true;
        this.origin = user.getLocation();
    }

    private boolean isCharged() {
        return System.currentTimeMillis() >= this.startTime + config.chargeTime;
    }

    private void render() {
        for (double theta = 0; theta <= Math.toRadians(180); theta += Math.toRadians(10)) {
            for (double phi = 0; phi < Math.toRadians(360); phi += Math.toRadians(10)) {
                double x = distance * Math.cos(phi) * Math.sin(theta);
                double y = distance * Math.sin(phi) * Math.sin(theta);
                double z = distance * Math.cos(theta);

                Vector3D direction = new Vector3D(x, y, z);
                Location eye = origin.add(0, 1.8, 0);
                Location location = origin.add(direction);

                if (this.conal) {
                    double rads = Vector3D.angle(direction, userDirection);
                    if (Math.abs(rads) > Math.toRadians(config.coneAngle)) {
                        continue;
                    }
                }

                if (!MaterialUtil.isIgnitable(location.getBlock())) {
                    continue;
                }

                Ray ray = new Ray(eye, location.add(0.5, 0.9, 0.5).subtract(eye).toVector().normalize());
                Location casted = RayCaster.cast(user.getWorld(), ray, distance, true);

                if (casted.distanceSquared(eye) < distance * distance) {
                    continue;
                }

                FireBlast.igniteBlocks(user, location);
            }
        }
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
        return "FireBurst";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    private static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double range;
        public double speed;
        public long chargeTime;
        public int coneAngle;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "fireburst");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(10000);
            range = abilityNode.getNode("range").getDouble(14.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            chargeTime = abilityNode.getNode("charge-time").getLong(3500);
            coneAngle = abilityNode.getNode("cone-angle").getInt(30);
        }
    }
}
