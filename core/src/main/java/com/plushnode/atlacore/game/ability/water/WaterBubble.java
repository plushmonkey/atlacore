package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.HashSet;
import java.util.Set;

public class WaterBubble implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private ActivePolicy activePolicy;
    private Set<Block> blocks = new HashSet<>();
    private Block block;
    private double radius;
    private double velocity;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        recalculateConfig();

        for (WaterBubble instance : Game.getAbilityInstanceManager().getPlayerInstances(user, WaterBubble.class)) {
            instance.updateActivePolicy(method);
            return false;
        }

        this.radius = 0;
        this.velocity = userConfig.speed;
        this.block = user.getLocation().getBlock();

        if (method == ActivationMethod.Sneak) {
            this.activePolicy = new SneakActivePolicy();
        } else {
            this.activePolicy = new TimedActivePolicy(userConfig.clickDuration);
        }

        return canActivate();
    }

    // Checks if the player is in the air before activating when the config dictates it.
    private boolean canActivate() {
        if (!userConfig.requireAir) return true;

        Block eyeBlock = user.getEyeLocation().getBlock();

        return MaterialUtil.isTransparent(eyeBlock) && !eyeBlock.isLiquid();
    }

    // If the current policy is timed and the player is punch activating, refresh it.
    // If the current policy is timed and the player is sneak activating, switch it to sneak activated.
    private void updateActivePolicy(ActivationMethod activationMethod) {
        if (!(activePolicy instanceof TimedActivePolicy)) return;

        if (activationMethod == ActivationMethod.Sneak) {
            activePolicy = new SneakActivePolicy();
        } else {
            ((TimedActivePolicy)activePolicy).refresh(userConfig.clickDuration);
        }
    }

    @Override
    public UpdateResult update() {
        if (this.velocity > 0 && !this.activePolicy.isActive()) {
            this.velocity = -userConfig.speed;
        }

        this.radius = Math.min(this.radius + this.velocity, userConfig.radius);

        if (this.radius <= 0) {
            return UpdateResult.Remove;
        }

        // Update the active set when increasing radius, decreasing radius, or when the player moves.
        if (this.radius < userConfig.radius || !this.block.equals(user.getLocation().getBlock())) {
            updateSet();
        }

        this.block = user.getLocation().getBlock();

        return UpdateResult.Continue;
    }

    // Reverts any blocks outside of the radius and adds new ones to the active set.
    private void updateSet() {
        // Go through current set and remove any blocks outside of the radius
        blocks.removeIf(block -> {
            if (block.getLocation().distanceSquared(user.getLocation()) > this.radius * this.radius) {
                revertBlock(block);

                return true;
            }
            return false;
        });

        // Update the active block set with any nearby water/waterlogged blocks.
        for (Block block : WorldUtil.getNearbyBlocks(user.getLocation(), this.radius, Material.AIR)) {
            if (blocks.contains(block)) continue;
            if (Game.getTempBlockService().isTempBlock(block)) continue;

            // TODO: Waterlogged

            if (block.getType() == Material.WATER) {
                new TempBlock(block, Material.AIR, Material.AIR.createBlockData(), false);
                blocks.add(block);
            }
        }
    }

    // Determines how the block should be reverted.
    // If it was changed, remove it from TempBlock list without reverting, otherwise normal revert.
    private void revertBlock(Block block) {
        TempBlock tempBlock = Game.getTempBlockService().getTempBlock(block);

        if (tempBlock != null) {
            if (hasChanged(block, tempBlock)) {
                Game.getTempBlockService().remove(tempBlock);
            } else {
                tempBlock.reset();
            }
        }
    }

    // Determines if the block has been changed since it was originally added.
    private boolean hasChanged(Block block, TempBlock tempBlock) {
        BlockState state = tempBlock.getPreviousState();

        if (state.getType() == Material.WATER) {
            // If the block was water before but isn't air now, then it must have changed.
            return block.getType() != Material.AIR;
        }

        // If the previous type wasn't water, then it was waterlogged and its current type must match previous.
        return state.getType() != block.getType();
    }


    @Override
    public void destroy() {
        for (Block block : blocks) {
            revertBlock(block);
        }

        blocks.clear();
    }

    private interface ActivePolicy {
        boolean isActive();
    }

    private class BaseActivePolicy implements ActivePolicy {
        @Override
        public boolean isActive() {
            return user.canBend(getDescription()) && block.getWorld().equals(user.getWorld());
        }
    }

    private class TimedActivePolicy extends BaseActivePolicy {
        private long endTime;

        TimedActivePolicy(long duration) {
            this.endTime = System.currentTimeMillis() + duration;
        }

        @Override
        public boolean isActive() {
            return super.isActive() && System.currentTimeMillis() < endTime;
        }

        void refresh(long duration) {
            this.endTime = System.currentTimeMillis() + duration;
        }
    }

    private class SneakActivePolicy extends BaseActivePolicy {
        @Override
        public boolean isActive() {
            return super.isActive() && user.isSneaking();
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterBubble";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public boolean requireAir;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.DURATION)
        public int clickDuration;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterbubble");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            requireAir = abilityNode.getNode("require-air").getBoolean(false);
            speed = abilityNode.getNode("speed").getDouble(0.5);
            radius = abilityNode.getNode("radius").getDouble(4.0);
            clickDuration = abilityNode.getNode("click").getNode("duration").getInt(2000);
        }
    }
}
