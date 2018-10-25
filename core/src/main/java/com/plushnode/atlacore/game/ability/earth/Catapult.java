package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Catapult implements Ability {
    private static final long TEMPBLOCK_DURATION = 4000;
    private static final long TEMPBLOCK_DELAY = 250;
    public static Config config = new Config();

    private User user;
    private boolean launched;
    private Block base;
    private long startTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.launched = false;
        this.startTime = System.currentTimeMillis();

        base = this.user.getLocation().getBlock().getRelative(BlockFace.DOWN);
        return MaterialUtil.isEarthbendable(base);
    }

    @Override
    public UpdateResult update() {
        if (!launched) {
            Vector3D direction = user.getDirection();

            if (Vector3D.angle(Vector3D.PLUS_J, direction) > Math.toRadians(config.angle)) {
                direction = Vector3D.PLUS_J;
            }

            Location target = user.getLocation().add(direction.scalarMultiply(config.strength));
            Vector3D force = target.subtract(user.getLocation()).toVector();

            user.setVelocity(force);
            user.setCooldown(this);
            launched = true;
        }

        if (System.currentTimeMillis() >= this.startTime + TEMPBLOCK_DELAY) {
            int length = getLength(base, 2);
            raiseEarth(base, length);
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    private int getLength(Block base, int maxLength) {
        for (int i = 0; i < maxLength; ++i) {
            Block current = base.getRelative(BlockFace.DOWN, i);

            if (!MaterialUtil.isEarthbendable(current)) {
                return i;
            }

            if (!Game.getProtectionSystem().canBuild(user, current.getLocation())) {
                return i;
            }
        }

        return maxLength;
    }

    private void raiseEarth(Block block, int length) {
        for (int i = 0; i < length; ++i) {
            Block current = block.getRelative(BlockFace.DOWN, i);
            Block newBlock = current.getRelative(BlockFace.UP, length);

            if (!Game.getProtectionSystem().canBuild(user, current.getLocation())) {
                return;
            }

            if (!Game.getProtectionSystem().canBuild(user, newBlock.getLocation())) {
                return;
            }

            new TempBlock(newBlock, current.getType(), TEMPBLOCK_DURATION);
            new TempBlock(current, Material.AIR, TEMPBLOCK_DURATION);
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
        return "Catapult";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        int angle;
        double strength;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "catapult");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(3000);
            angle = abilityNode.getNode("angle").getInt(80);
            strength = abilityNode.getNode("strength").getDouble(2.5);
        }
    }
}
