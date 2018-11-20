package com.plushnode.atlacore.game.ability.earth.passives;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.PassiveAbility;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DensityShift implements PassiveAbility {
    public static Config config = new Config();

    private User user;
    private Config userConfig;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        return UpdateResult.Continue;
    }

    public static boolean isSoftened(User user) {
        if (Game.getAbilityInstanceManager().getPlayerInstances(user, DensityShift.class).isEmpty()) {
            return false;
        }

        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName("DensityShift");

        if (!user.canBend(desc)) {
            return false;
        }

        Collection<Block> nearbyBlocks = WorldUtil.getNearbyBlocks(user.getLocation(), 3.0, Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR));

        // It seems to be possible to activate fall damage 0.4999 blocks above, so extend it down 0.5 to find collisions.
        Location location = user.getLocation().subtract(0, 0.5, 0);
        AABB userBounds = user.getBounds().at(location);

        if (isColliding(userBounds, nearbyBlocks)) {
            return true;
        }

        Location previous = user.getLocation().subtract(user.getVelocity());

        Ray ray = new Ray(previous, user.getVelocity().normalize());
        Block hit = RayCaster.blockCast(user.getWorld(), ray, user.getVelocity().getNorm(), true);

        return hit != null && MaterialUtil.isEarthbendable(hit);
    }

    private static boolean isColliding(Collider userBounds, Collection<Block> blocks) {
        for (Block block : blocks) {
            if (!MaterialUtil.isEarthbendable(block)) continue;
            if (!block.hasBounds()) continue;

            AABB bounds = block.getBounds().at(block.getLocation());

            // Horizontally adjacent blocks aren't sufficient for softening a landing.
            if (block.getLocation().getY() > userBounds.getPosition().getY()) continue;

            if (userBounds.intersects(bounds)) {
                return true;
            }
        }

        return false;
    }

    public static void softenArea(User user, Location location) {
        List<DensityShift> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, DensityShift.class);
        if (instances.isEmpty()) return;

        DensityShift instance = instances.get(0);

        List<Block> nearby = WorldUtil.getNearbyBlocks(location, instance.userConfig.radius).stream()
                .filter((b) -> !b.getRelative(BlockFace.UP).hasBounds())
                .filter(MaterialUtil::isEarthbendable)
                .filter((b) -> !Game.getTempBlockService().isTempBlock(b.getRelative(BlockFace.DOWN)))
                .collect(Collectors.toList());

        for (Block block : nearby) {
            Block above = block.getRelative(BlockFace.UP);

            if (MaterialUtil.isTransparent(above)) {
                new TempBlock(above, Material.AIR, instance.userConfig.duration);
            }

            Material type = Material.SAND;

            if (MaterialUtil.isTransparent(block.getRelative(BlockFace.DOWN))) {
                type = Material.SANDSTONE;
            }

            new TempBlock(block, type, instance.userConfig.duration);
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
        return "DensityShift";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.RADIUS)
        public double radius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "passives", "densityshift");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            duration = abilityNode.getNode("duration").getLong(6000);
            radius = abilityNode.getNode("radius").getDouble(2.0);
        }
    }
}
