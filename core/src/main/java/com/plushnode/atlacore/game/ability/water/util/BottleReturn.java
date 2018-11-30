package com.plushnode.atlacore.game.ability.water.util;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.source.SourceUtil;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class BottleReturn implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Location location;
    private TempBlock tempBlock;

    public BottleReturn() {
        this.location = null;
        this.tempBlock = null;
    }

    public BottleReturn(Location origin) {
        this.location = origin;
        this.tempBlock = null;
    }

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        return this.location != null;
    }

    @Override
    public UpdateResult update() {
        if (this.tempBlock != null) {
            this.tempBlock.reset();
            this.tempBlock = null;
        }

        if (!user.getWorld().equals(location.getWorld())) {
            return UpdateResult.Remove;
        }

        Location target = user.getEyeLocation();
        double distSq = location.distanceSquared(target);

        if (distSq <= userConfig.speed * userConfig.speed) {
            SourceUtil.fillBottle(user);
            return UpdateResult.Remove;
        }

        if (distSq > userConfig.maxDistance * userConfig.maxDistance) {
            return UpdateResult.Remove;
        }

        Vector3D direction = target.subtract(location).toVector().normalize();

        location = location.add(direction.scalarMultiply(userConfig.speed));

        Block block = location.getBlock();
        if (!MaterialUtil.isTransparent(block) && block.getType() != Material.WATER && block.getType() != Material.ICE) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        this.tempBlock = new TempBlock(block, Material.WATER);

        return UpdateResult.Continue;
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
        return "BottleReturn";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public double maxDistance;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("properties", "bottlebending");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            maxDistance = abilityNode.getNode("max-distance").getDouble(40.0);
        }
    }
}
