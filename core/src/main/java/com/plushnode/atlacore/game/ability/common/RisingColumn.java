package com.plushnode.atlacore.game.ability.common;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;

import java.util.ArrayList;
import java.util.List;

public class RisingColumn {
    private User user;
    private Block base;
    private int height;
    private int maxHeight;
    private int duration;
    private int currentHeight;
    private boolean finished;
    private boolean waitUntilFinished;
    private long lastTime;
    private List<Block> affected = new ArrayList<>();

    public RisingColumn(User user, Block base, int maxHeight, int duration, boolean waitUntilFinished) {
        this.user = user;
        this.base = base;
        this.currentHeight = 0;
        this.maxHeight = maxHeight;
        this.duration = duration;
        this.finished = false;
        this.waitUntilFinished = waitUntilFinished;

        this.height = getHeight();
    }

    public boolean isValid() {
        return this.height > 0;
    }

    public boolean update() {
        long time = System.currentTimeMillis();

        if (finished) {
            if (waitUntilFinished) {
                return time >= this.lastTime + duration + 1000;
            } else {
                return true;
            }
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

        if (++currentHeight >= this.height) {
            finished = true;
        }

        return false;
    }

    private int getHeight() {
        for (int i = 0; i < maxHeight; ++i) {
            Block check = this.base.getRelative(BlockFace.DOWN, i);

            if (!MaterialUtil.isEarthbendable(check)) {
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
            new TempBlock(newBlock, MaterialUtil.getSolidEarthType(current.getType()), duration);
        }

        new TempBlock(top.getRelative(BlockFace.DOWN, height - 1), Material.AIR, duration);
        return true;
    }

    public List<Block> getAffected() {
        return affected;
    }
}
