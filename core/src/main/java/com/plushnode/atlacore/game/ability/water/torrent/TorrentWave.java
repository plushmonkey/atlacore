package com.plushnode.atlacore.game.ability.water.torrent;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TorrentWave implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private double radius;
    private Location origin;
    private List<TempBlock> tempBlocks = new ArrayList<>();
    private List<List<Double>> angles = new ArrayList<>();
    private List<Entity> affectedEntities = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.radius = 3;
        this.origin = user.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);

        double delta = (Math.PI * 2) / (8 * userConfig.radius);

        for (int i = 0; i < userConfig.height; ++i) {
            List<Double> current = new ArrayList<>();

            for (double theta = 0; theta < Math.PI * 2; theta += delta) {
                current.add(theta);
            }

            angles.add(current);
        }

        if (user.isOnCooldown(getDescription())) {
            return false;
        }

        user.setCooldown(this, userConfig.cooldown);

        return true;
    }

    @Override
    public UpdateResult update() {
        this.radius = Math.min(this.radius + userConfig.speed, userConfig.radius);

        render();

        if (this.radius >= userConfig.radius) {
            clear();
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    private void clear() {
        tempBlocks.forEach(TempBlock::reset);
        tempBlocks.clear();
    }

    private void render() {
        clear();

        for (int height = 0; height < userConfig.height; ++height) {
            List<Double> currentAngles = angles.get(height);
            // Store the index of any obstructed angles to pop them off the angles list later.
            List<Integer> obstructions = new ArrayList<>();

            for (int i = 0; i < currentAngles.size(); ++i) {
                double theta = currentAngles.get(i);

                double x = Math.cos(theta) * radius;
                double z = Math.sin(theta) * radius;

                Location location = origin.add(x, height, z).getBlock().getLocation();
                Block block = location.getBlock();

                if (Game.getProtectionSystem().canBuild(user, location) && (MaterialUtil.isTransparent(block) || block.getType() == Material.WATER)) {
                    tempBlocks.add(new TempBlock(block, Material.WATER));
                } else {
                    obstructions.add(i);
                }

                CollisionUtil.handleEntityCollisions(user, AABB.BLOCK_BOUNDS.at(location), entity -> {
                    if (affectedEntities.contains(entity)) return false;

                    affectedEntities.add(entity);

                    if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) return false;

                    Vector3D direction = entity.getLocation().subtract(origin).toVector();
                    direction = VectorUtil.normalizeOrElse(VectorUtil.setY(direction, 0), Vector3D.PLUS_I);

                    entity.setVelocity(entity.getVelocity().add(direction.scalarMultiply(userConfig.knockback)));

                    return false;
                }, true);
            }

            // Do swap removal for the obstructions to prevent array shifting.
            for (int i = 0; i < obstructions.size(); ++i) {
                int index = obstructions.get(i);
                int endIndex = currentAngles.size() - i - 1;

                Collections.swap(currentAngles, index, endIndex);
            }

            // Pop off the end of the currentAngles for however many obstructions were swapped to the end.
            for (int i = 0; i < obstructions.size(); ++i) {
                currentAngles.remove(currentAngles.size() - 1);
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
        return "TorrentWave";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        // Don't recalculate config. Any changes made during active instance won't change it.
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.HEIGHT)
        public int height;
        @Attribute(Attributes.STRENGTH)
        public double knockback;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "torrent", "wave");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            radius = abilityNode.getNode("radius").getDouble(12.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            height = abilityNode.getNode("height").getInt(3);
            knockback = abilityNode.getNode("knockback").getDouble(1.5);
        }
    }
}
