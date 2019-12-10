package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
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
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhaseChange implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Set<TempBlock> frozen = new HashSet<>();
    private Block lastUserBlock;
    private long lastBlockCheck;
    private RemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.lastUserBlock = user.getEyeLocation().getBlock();
        this.lastBlockCheck = System.currentTimeMillis();
        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new IsOfflineRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user)
        );

        PhaseChange instance = Game.getAbilityInstanceManager().getFirstInstance(user, PhaseChange.class);

        if (instance == null) {
            instance = this;
        }

        instance.recalculateConfig();

        if (method == ActivationMethod.Punch) {
            instance.freeze();
        } else if (method == ActivationMethod.Sneak) {
            instance.melt();
            return false;
        }

        return instance == this;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (time > this.lastBlockCheck + 1000 || !user.getEyeLocation().getBlock().equals(this.lastUserBlock)) {
            manageFrozenBlocks();
        }

        return frozen.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
    }

    private void manageFrozenBlocks() {
        double rangeSq = userConfig.freezeActiveRange * userConfig.freezeActiveRange;

        this.lastUserBlock = user.getEyeLocation().getBlock();
        this.lastBlockCheck = System.currentTimeMillis();

        List<TempBlock> removal = new ArrayList<>();

        for (TempBlock tempBlock : frozen) {
            if (tempBlock.getBlock().getType() != Material.ICE) {
                removal.add(tempBlock);
                continue;
            }

            Location center = tempBlock.getBlock().getLocation().add(0.5, 0.5, 0.5);

            if (center.distanceSquared(user.getEyeLocation()) > rangeSq) {
                tempBlock.reset();
                removal.add(tempBlock);
            }
        }

        frozen.removeAll(removal);
    }

    private void freeze() {
        Location location = RayCaster.cast(user, user.getViewRay(), userConfig.freezeSelectRange, true, false);

        for (Block block : WorldUtil.getNearbyType(location, userConfig.freezeRadius, Material.WATER)) {
            if (!MaterialUtil.isAir(block.getRelative(BlockFace.UP))) continue;
            if (Game.getTempBlockService().isTempBlock(block)) continue;

            if (Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                TempBlock tempBlock = new TempBlock(block, Material.ICE);

                frozen.add(tempBlock);
            }
        }
    }

    private void melt() {
        Location location = RayCaster.cast(user, user.getViewRay(), userConfig.meltSelectRange, true, false);

        for (Block block : WorldUtil.getNearbyType(location, userConfig.meltRadius, Material.ICE)) {
            if (Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                TempBlock tempBlock = Game.getTempBlockService().getTempBlock(block);

                frozen.remove(tempBlock);

                if (tempBlock != null) {
                    tempBlock.reset();
                } else {
                    block.setType(Material.WATER);
                }
            }
        }
    }

    @Override
    public void destroy() {
        for (TempBlock tempBlock : frozen) {
            tempBlock.reset();
        }

        frozen.clear();
    }

    public static boolean isFrozenBlock(TempBlock tempBlock) {
        for (PhaseChange instance : Game.getAbilityInstanceManager().getInstances(PhaseChange.class)) {
            if (instance.frozen.contains(tempBlock)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public String getName() {
        return "PhaseChange";
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

        @Attribute(Attributes.SELECTION)
        public double freezeSelectRange;
        @Attribute(Attributes.RADIUS)
        public double freezeRadius;
        @Attribute(Attributes.STRENGTH)
        public double freezeActiveRange;

        @Attribute(Attributes.SELECTION)
        public double meltSelectRange;
        @Attribute(Attributes.RADIUS)
        public double meltRadius;


        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "phasechange");

            enabled = abilityNode.getNode("enabled").getBoolean(true);

            freezeSelectRange = abilityNode.getNode("freeze").getNode("select-range").getDouble(7.0);
            freezeActiveRange = abilityNode.getNode("freeze").getNode("active-range").getDouble(25.0);
            freezeRadius = abilityNode.getNode("freeze").getNode("radius").getDouble(4.0);

            meltSelectRange = abilityNode.getNode("melt").getNode("select-range").getDouble(7.0);
            meltRadius = abilityNode.getNode("melt").getNode("radius").getDouble(4.0);
        }
    }
}
