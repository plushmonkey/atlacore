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
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Collapse implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private List<Column> columns = new ArrayList<>();
    private int maxHeight;
    private int current;
    private long nextUpdateTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);

        Block source = getSource();

        if (source == null) {
            return false;
        }

        if (method == ActivationMethod.Punch) {
            if (createColumn(source)) {
                user.setCooldown(this, userConfig.punchCooldown);
            }
        } else {
            for (Block block : WorldUtil.getNearbyBlocks(source.getLocation(), userConfig.radius, Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR))) {
                if (!MaterialUtil.isEarthbendable(block)) continue;

                boolean unique = !columns.stream()
                        .anyMatch((c) -> c.base.getX() == block.getX() && c.base.getZ() == block.getZ());

                if (unique) {
                    createColumn(block);
                }
            }

            if (!columns.isEmpty()) {
                user.setCooldown(this, userConfig.sneakCooldown);
            }
        }

        return !columns.isEmpty();
    }

    private boolean createColumn(Block source) {
        Block base = getBase(source);

        if (base == null) {
            return false;
        }

        int height = getHeight(base);

        if (height > 0) {
            columns.add(new Column(base, height));

            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        return height > 0;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (time > nextUpdateTime) {
            for (Column column : columns) {
                column.update(current);
            }

            ++current;
            nextUpdateTime = time + 150;
        }

        return current > maxHeight ? UpdateResult.Remove : UpdateResult.Continue;
    }

    @Override
    public void destroy() {

    }

    private Block getSource() {
        Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.selectRange, true);

        if (block == null || !MaterialUtil.isEarthbendable(block)) return null;
        if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) return null;

        return block;
    }

    private Block getBase(Block block) {
        for (int i = 0; i < userConfig.height; ++i) {
            Block below = block.getRelative(BlockFace.DOWN);

            if (!below.hasBounds() && !below.isLiquid()) {
                return block;
            }

            if (!MaterialUtil.isEarthbendable(below)) {
                return null;
            }

            block = below;
        }

        return null;
    }

    private int getHeight(Block base) {
        int height = userConfig.height;

        // Check for empty spaces below the base.
        for (int i = 0; i < userConfig.height; ++i) {
            Block current = base.getRelative(BlockFace.DOWN, i + 1);

            if (current.isLiquid() || current.hasBounds()) {
                height = i + 1;
                break;
            }
        }

        // Check above the base for earthbendable blocks to shift down.
        for (int i = 1; i <= height; ++i) {
            Block current = base.getRelative(BlockFace.UP, i);

            if (!MaterialUtil.isEarthbendable(current)) {
                height = i + 1;
                break;
            }
        }

        return height;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Collapse";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public static class Column {
        Block base;
        int height;

        Column(Block base, int height) {
            this.base = base;
            this.height = height;
        }

        void update(int current) {
            if (current >= height) return;

            for (int i = 0; i < height; ++i) {
                Block below = base.getRelative(BlockFace.UP, i - 1);
                Block above = base.getRelative(BlockFace.UP, i);

                if (!below.hasBounds() && !below.isLiquid()) {
                    new TempBlock(below, above.getType(), 10000);
                    new TempBlock(above, Material.AIR, 10000);
                }
            }

            base = base.getRelative(BlockFace.DOWN);
        }
    }

    private static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.HEIGHT)
        public int height;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.COOLDOWN)
        public long punchCooldown;
        @Attribute(Attributes.COOLDOWN)
        public long sneakCooldown;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "collapse");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            selectRange = abilityNode.getNode("select-range").getDouble(10.0);
            height = abilityNode.getNode("height").getInt(6);
            radius = abilityNode.getNode("radius").getDouble(7.0);
            punchCooldown = abilityNode.getNode("punch-cooldown").getLong(1000);
            sneakCooldown = abilityNode.getNode("sneak-cooldown").getLong(4000);
        }
    }
}
