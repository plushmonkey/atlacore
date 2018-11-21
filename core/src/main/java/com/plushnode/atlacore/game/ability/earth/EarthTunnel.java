package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class EarthTunnel implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private long nextUpdate;
    private double distance;
    private int angle;
    private double radius;
    private Location center;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();
        this.nextUpdate = System.currentTimeMillis();
        this.angle = 0;
        this.radius = 0;

        Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.maxDistance, true);

        if (block != null && MaterialUtil.isEarthbendable(block)) {
            if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                return false;
            }

            this.center = block.getLocation().add(0.5, 0.5, 0.5);
            this.distance = 0.0;

            return true;

        }

        return false;
    }

    @Override
    public UpdateResult update() {
        if (!user.isSneaking()) {
            return UpdateResult.Remove;
        }

        long time = System.currentTimeMillis();

        if (time < this.nextUpdate) {
            return UpdateResult.Continue;
        }

        // Allow it to update multiple times within one tick to catch up to interval.
        while (time >= this.nextUpdate) {
            this.nextUpdate += Math.round(userConfig.interval / 2.0);

            if (this.distance > userConfig.maxDistance) {
                return UpdateResult.Remove;
            }

            Vector3D offset = getOffset(user.getDirection(), Math.toRadians(angle), radius);
            Block current = center.add(offset).getBlock();

            if (current != null) {
                if (!Game.getProtectionSystem().canBuild(user, current.getLocation())) {
                    return UpdateResult.Remove;
                }

                if (MaterialUtil.isEarthbendable(current)) {
                    if (userConfig.revert) {
                        new TempBlock(current, Material.AIR, 30000);
                    } else {
                        Game.plugin.getBlockSetter().setBlock(current, Material.AIR);
                    }
                }
            }

            if (angle >= 360) {
                angle = 0;

                Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.maxDistance, true);

                if (block == null) {
                    return UpdateResult.Remove;
                }

                this.center = block.getLocation().add(0.5, 0.5, 0.5);

                if (++this.radius > userConfig.radius) {
                    radius = 0.0;

                    if (++distance > userConfig.maxDistance) {
                        return UpdateResult.Remove;
                    }
                }
            } else {
                // Immediately increase radius
                if (radius <= 0.0) {
                    ++radius;
                } else {
                    angle += 360 / (radius * 16);
                }
            }
        }

        return UpdateResult.Continue;
    }

    private static Vector3D getOffset(Vector3D axis, double rads, double length) {
        Vector3D ortho = new Vector3D(axis.getY(), -axis.getX(), 0);
        ortho = VectorUtil.normalizeOrElse(ortho, Vector3D.PLUS_I);
        ortho = ortho.scalarMultiply(length);

        return VectorUtil.rotate(ortho, axis, rads);
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
        return "EarthTunnel";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);

        this.userConfig.interval = Math.max(this.userConfig.interval, 1);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.RANGE)
        public double maxDistance;
        @Attribute(Attributes.CHARGE_TIME)
        public long interval;
        @Attribute(Attributes.RADIUS)
        public int radius;
        public boolean revert;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "earthtunnel");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(1000);
            maxDistance = abilityNode.getNode("max-distance").getDouble(10.0);
            interval = abilityNode.getNode("interval").getLong(30);
            radius = abilityNode.getNode("radius").getInt(1);
            revert = abilityNode.getNode("revert").getBoolean(true);
        }
    }
}
