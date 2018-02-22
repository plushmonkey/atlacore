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
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RaiseEarth implements Ability {
    public static Config config = new Config();

    private User user;
    private List<Block> affected = new ArrayList<>();
    private List<Column> columns = new ArrayList<>();
    private long duration;
    private long interval;
    private int maxHeight;
    private long lastTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        boolean wall = method == ActivationMethod.Sneak;

        double selectRange;
        long cooldown;
        int width = 1;

        if (wall) {
            cooldown = config.wallCooldown;
            this.duration = config.wallDuration;
            this.interval = config.wallInterval;
            this.maxHeight = config.wallMaxHeight;
            selectRange = config.wallSelectRange;
            width = config.wallWidth;
        } else {
            cooldown = config.columnCooldown;
            this.duration = config.columnDuration;
            this.interval = config.columnInterval;
            this.maxHeight = config.columnMaxHeight;
            selectRange = config.columnSelectRange;
        }

        Block selection = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), selectRange, true);
        if (selection == null || !MaterialUtil.isEarthbendable(selection)) {
            return false;
        }

        if (!createCenterColumn(selection)) {
            return false;
        }

        if (wall) {
            addWallColumns(selection, width);
        }

        user.setCooldown(this, cooldown);

        return true;
    }

    private boolean createCenterColumn(Block selection) {
        Block base = getBase(selection);

        if (base == null || !MaterialUtil.isEarthbendable(base) || isRaisedBlock(base)) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, base.getLocation())) {
            return false;
        }

        Column column = new Column(base);

        columns.add(column);

        return column.isValid();
    }

    private void addWallColumns(Block selection, int width) {
        int firstSideSize = (int)Math.ceil((width - 1) / 2.0);
        int secondSideSize = (int)Math.floor((width - 1) / 2.0);
        Vector3D side = user.getDirection().crossProduct(Vector3D.PLUS_J).normalize();

        Location location = selection.getLocation().add(0.5, 0.5, 0.5);
        for (int i = 0; i < firstSideSize; ++i) {
            Block check = location.add(side.scalarMultiply(i + 1)).getBlock();
            Block base = getBase(check);

            if (base == null || !MaterialUtil.isEarthbendable(base) || isRaisedBlock(base)) {
                continue;
            }

            columns.add(new Column(base));
        }

        for (int i = 0; i < secondSideSize; ++i) {
            Block check = location.add(side.scalarMultiply((i + 1) * -1)).getBlock();
            Block base = getBase(check);

            if (base == null || !MaterialUtil.isEarthbendable(base) || isRaisedBlock(base)) {
                continue;
            }

            columns.add(new Column(base));
        }
    }

    private Block getBase(Block selection) {
        for (int i = 0; i < maxHeight; ++i) {
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

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (time > lastTime + interval) {
            affected.clear();

            for (Iterator<Column> iterator = columns.iterator(); iterator.hasNext(); ) {
                Column column = iterator.next();
                if (!column.update()) {
                    iterator.remove();
                    continue;
                }

                affected.addAll(column.affected);
            }

            this.lastTime = time;
        }

        return columns.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
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

    private class Column {
        private Block base;
        private int height;
        private int currentHeight;
        private boolean finished;
        private long lastTime;
        private List<Block> affected = new ArrayList<>();

        public Column(Block base) {
            this.base = base;
            this.currentHeight = 0;
            this.finished = false;

            this.height = getHeight();
        }

        public boolean isValid() {
            return this.height > 0;
        }

        public boolean update() {
            long time = System.currentTimeMillis();

            if (finished) {
                return time < this.lastTime + duration + 1000;
            }

            affected.clear();

            this.lastTime = time;

            Block top = base.getRelative(BlockFace.UP, currentHeight);
            Block aboveTop = top.getRelative(BlockFace.UP);

            // Move entities into the air when standing on top of the RaiseEarth.
            AABB collider = AABB.BLOCK_BOUNDS.at(aboveTop.getLocation());
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                entity.setVelocity(VectorUtil.setY(entity.getVelocity(), 1));
                return false;
            }, true, true);

            if (!raise(top, height, duration)) {
                currentHeight = height;
                finished = true;
            }

            if (++currentHeight >= maxHeight) {
                finished = true;
            }

            return true;
        }

        private int getHeight() {
            for (int i = 0; i < maxHeight; ++i) {
                Block check = this.base.getRelative(BlockFace.DOWN, i);

                if (!MaterialUtil.isEarthbendable(check) && check.getType() != Material.AIR) {
                    return i;
                }

                if (!Game.getProtectionSystem().canBuild(user, check.getLocation())) {
                    return i;
                }
            }

            return maxHeight;
        }

        private boolean raise(Block top, int height, long duration) {
            if (top.getRelative(BlockFace.UP).hasBounds()) {
                return false;
            }

            for (int i = 0; i < height; ++i) {
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

            new TempBlock(top.getRelative(BlockFace.DOWN, height - 1), Material.AIR, duration);
            return true;
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long columnCooldown;
        long columnDuration;
        long columnInterval;
        int columnMaxHeight;
        double columnSelectRange;
        long wallCooldown;
        long wallDuration;
        long wallInterval;
        int wallMaxHeight;
        double wallSelectRange;
        int wallWidth;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "raiseearth");

            enabled = abilityNode.getNode("enabled").getBoolean(true);

            CommentedConfigurationNode columnNode = abilityNode.getNode("column");

            columnCooldown = columnNode.getNode("cooldown").getLong(500);
            columnDuration = columnNode.getNode("duration").getLong(6000);
            columnInterval = columnNode.getNode("interval").getLong(100);
            columnMaxHeight = columnNode.getNode("max-height").getInt(6);
            columnSelectRange = columnNode.getNode("select-range").getDouble(8.0);

            CommentedConfigurationNode wallNode = abilityNode.getNode("wall");
            wallCooldown = wallNode.getNode("cooldown").getLong(500);
            wallDuration = wallNode.getNode("duration").getLong(6000);
            wallInterval = wallNode.getNode("interval").getLong(100);
            wallMaxHeight = wallNode.getNode("max-height").getInt(6);
            wallSelectRange = wallNode.getNode("select-range").getDouble(8.0);
            wallWidth = wallNode.getNode("width").getInt(6);
        }
    }
}
