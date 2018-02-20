package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class RaiseEarth implements Ability {
    public static Config config = new Config();

    private User user;
    private Block base;
    private int length;
    private int currentLength;
    private long startTime;
    private List<Block> affected = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        Block selection = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), config.selectRange, true);
        if (selection == null || !MaterialUtil.isEarthbendable(selection)) {
            return false;
        }

        this.base = getBase(selection);
        if (this.base == null || !MaterialUtil.isEarthbendable(this.base)) {
            return false;
        }

        if (isRaisedBlock(this.base)) {
            return false;
        }

        this.length = getLength();

        if (this.length > 0) {
            if (!Game.getProtectionSystem().canBuild(user, base.getLocation())) {
                return false;
            }

            user.setCooldown(this);
            return true;
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (currentLength >= length) {
            // Keep it around long enough for the TempBlocks to time out.
            // This is so isRaisedBlock can check the affected blocks.
            if (time >= startTime + config.duration + 1000) {
                return UpdateResult.Remove;
            }
            return UpdateResult.Continue;
        }

        if (time >= startTime + config.interval) {
            Block top = base.getRelative(BlockFace.UP, currentLength);
            Block aboveTop = top.getRelative(BlockFace.UP);

            // Move entities into the air when standing on top of the RaiseEarth.
            AABB collider = AABB.BLOCK_BOUNDS.at(aboveTop.getLocation());
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                entity.setVelocity(VectorUtil.setY(entity.getVelocity(), 1));
                return false;
            }, true, true);

            if (!raise(top, length, config.duration)) {
                currentLength = length;
            }

            ++currentLength;
            // Keep restarting startTime for every movement.
            this.startTime = time;
        }

        return UpdateResult.Continue;
    }

    private Block getBase(Block selection) {
        for (int i = 0; i < config.maxLength; ++i) {
            Block check = selection.getRelative(BlockFace.UP, i);

            if (!MaterialUtil.isEarthbendable(check)) {
                if (check.hasBounds()) {
                    return null;
                }

                return check.getRelative(BlockFace.DOWN);
            }
        }

        return null;
    }

    private int getLength() {
        for (int i = 0; i < config.maxLength; ++i) {
            Block check = this.base.getRelative(BlockFace.DOWN, i);

            if (!MaterialUtil.isEarthbendable(check)) {
                return i;
            }

            if (!Game.getProtectionSystem().canBuild(user, check.getLocation())) {
                return i;
            }
        }

        return config.maxLength;
    }

    private boolean raise(Block top, int length, long duration) {
        affected.clear();

        if (top.getRelative(BlockFace.UP).hasBounds()) {
            return false;
        }

        for (int i = 0; i < length; ++i) {
            Block current = top.getRelative(BlockFace.DOWN, i);
            Block newBlock = current.getRelative(BlockFace.UP);

            if (!Game.getProtectionSystem().canBuild(user, current.getLocation())) {
                return false;
            }

            if (!Game.getProtectionSystem().canBuild(user, newBlock.getLocation())) {
                return false;
            }

            affected.add(newBlock);
            new TempBlock(newBlock, current.getType(), duration);
        }

        new TempBlock(top.getRelative(BlockFace.DOWN, length - 1), Material.AIR, duration);
        return true;
    }

    public static boolean isRaisedBlock(Block block) {
        return Game.getAbilityInstanceManager().getInstances().stream()
                .filter((a) -> a instanceof RaiseEarth)
                .anyMatch((re) -> ((RaiseEarth)re).affected.contains(block));
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
        return "RaiseEarth";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        long duration;
        long interval;
        int maxLength;
        double selectRange;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "raiseearth");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            duration = abilityNode.getNode("duration").getLong(6000);
            interval = abilityNode.getNode("interval").getLong(100);
            maxLength = abilityNode.getNode("max-length").getInt(6);
            selectRange = abilityNode.getNode("select-range").getDouble(8.0);
        }
    }
}
