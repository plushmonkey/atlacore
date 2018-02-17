package com.plushnode.atlacore.game.ability.earth.passives;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.PassiveAbility;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.List;
import java.util.stream.Collectors;

public class DensityShift implements PassiveAbility {
    public static Config config = new Config();

    private User user;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        return true;
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

        Block center = user.getLocation().getBlock().getRelative(BlockFace.DOWN);

        if (!MaterialUtil.isEarthbendable(center)) {
            return false;
        }

        List<Block> nearby = WorldUtil.getNearbyBlocks(center.getLocation(), config.radius).stream()
                .filter((b) -> b.getY() == center.getY())
                .filter(MaterialUtil::isEarthbendable)
                .filter((b) -> b.getRelative(BlockFace.DOWN).hasBounds())
                .collect(Collectors.toList());

        for (Block block : nearby) {
            new TempBlock(block, Material.SAND, config.duration);
        }

        return true;
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

    private static class Config extends Configurable {
        boolean enabled;
        long duration;
        double radius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "passives", "densityshift");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            duration = abilityNode.getNode("duration").getLong(6000);
            radius = abilityNode.getNode("radius").getDouble(2.0);
        }
    }
}
