package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.Arrays;
import java.util.function.Predicate;

public class HeatControl implements Ability {
    public static Config config = new Config();

    private User user;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        // Only implement melt and extinguish currently
        if (method != ActivationMethod.Punch) return false;

        this.user = user;

        if (melt()) {
            user.setCooldown(this, config.meltCooldown);
        }

        if (extinguish()) {
            user.setCooldown(this, config.extinguishCooldown);
        }

        return false;
    }

    private boolean melt() {
        return act(config.meltRange, config.meltRadius, this::isIce);
    }

    private boolean extinguish() {
        return act(config.extinguishRange, config.extinguishRadius, (b) -> b.getType() == Material.FIRE);
    }

    private boolean act(double range, double radius, Predicate<Block> predicate) {
        Ray ray = new Ray(user.getEyeLocation(), user.getDirection());
        Location location = RayCaster.cast(user.getWorld(), ray, range, false);

        boolean acted = false;

        for (Block block : WorldUtil.getNearbyBlocks(location, radius, Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR))) {
            if (!predicate.test(block)) continue;

            if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                continue;
            }

            TempBlock tempBlock = Game.getTempBlockService().getTempBlock(block);
            if (tempBlock != null) {
                tempBlock.reset();

                if (!predicate.test(block)) {
                    continue;
                }
            }

            acted = true;
            Game.plugin.getBlockSetter(BlockSetter.Flag.FAST).setBlock(block, Material.AIR);
        }

        return acted;
    }

    private boolean isIce(Block block) {
        return block.getType() == Material.ICE ||
                block.getType() == Material.PACKED_ICE ||
                block.getType() == Material.FROSTED_ICE;
    }

    public static boolean canBurn(User user) {
        if (!user.canBend(Game.getAbilityRegistry().getAbilityByName("HeatControl"))) return true;

        AbilityDescription current = user.getSelectedAbility();

        if (current != null && "HeatControl".equals(user.getSelectedAbility().getName())) {
            user.setFireTicks(0);
            return false;
        }

        if (user.getFireTicks() > config.maxFireTicks) {
            user.setFireTicks(config.maxFireTicks);
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        return UpdateResult.Remove;
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
        return "HeatControl";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Config extends Configurable {
        public boolean enabled;

        public int maxFireTicks;

        public long extinguishCooldown;
        public double extinguishRange;
        public double extinguishRadius;

        public long meltCooldown;
        public double meltRange;
        public double meltRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "heatcontrol");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            maxFireTicks = abilityNode.getNode("maxFireTicks").getInt(80);

            CommentedConfigurationNode extinguishNode = abilityNode.getNode("extinguish");

            extinguishCooldown = extinguishNode.getNode("cooldown").getLong(500);
            extinguishRange = extinguishNode.getNode("range").getDouble(20.0);
            extinguishRadius = extinguishNode.getNode("radius").getDouble(7.0);

            CommentedConfigurationNode meltNode = abilityNode.getNode("melt");

            meltCooldown = meltNode.getNode("cooldown").getLong(500);
            meltRange = meltNode.getNode("range").getDouble(15.0);
            meltRadius = meltNode.getNode("radius").getDouble(5.0);
        }
    }
}
