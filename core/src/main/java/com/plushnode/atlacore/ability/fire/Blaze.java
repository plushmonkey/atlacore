package com.plushnode.atlacore.ability.fire;

import com.plushnode.atlacore.*;
import com.plushnode.atlacore.ability.Ability;
import com.plushnode.atlacore.ability.ActivationMethod;
import com.plushnode.atlacore.block.Block;
import com.plushnode.atlacore.block.BlockFace;
import com.plushnode.atlacore.block.BlockState;
import com.plushnode.atlacore.block.Material;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.util.TempBlock;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class Blaze implements Ability {
    private User user;

    private Location location;
    private List<FireStream> fireStreams;

    @Override
    public boolean create(User user, ActivationMethod method) {
        System.out.println("Creating blaze for caster " + user);
        this.location = user.getLocation();
        this.fireStreams = new ArrayList<>();
        this.user = user;

        int arcBegin = 0;
        int arcEnd = 360;
        int stepSize = 10;

        if (method == ActivationMethod.Punch) {
            arcBegin = -16;
            arcEnd = 16;
            stepSize = 2;
        }

        for (double degrees = arcBegin; degrees < arcEnd; degrees += stepSize) {
            double angle = Math.toRadians(degrees);
            Vector3D direction = user.getDirection();

            double x, z, vx, vz;
            x = direction.getX();
            z = direction.getZ();
            vx = x * Math.cos(angle) - z * Math.sin(angle);
            vz = x * Math.sin(angle) + z * Math.cos(angle);

            direction = new Vector3D(vx, direction.getY(), vz);

            int range = 10;
            fireStreams.add(new FireStream(direction, range));
        }
        return true;
    }

    @Override
    public boolean update() {
        Iterator<FireStream> iterator = fireStreams.iterator();

        while (iterator.hasNext()) {
            FireStream fireStream = iterator.next();
            if (fireStream.update()) {
                iterator.remove();
                fireStream.destroy();
            }
        }

        return fireStreams.isEmpty();
    }

    @Override
    public void destroy() {
        for (FireStream fireStream : fireStreams) {
            fireStream.destroy();
        }
    }

    @Override
    public String getName() {
        return "Blaze";
    }

    private class FireStream {
        private final static int INTERVAL = 50;
        private final static int DURATION = 400;
        private double range;
        private long nextUpdate;
        private Location origin, currentLocation;
        private Vector3D direction;
        private boolean blocked;

        private Map<TempBlock, Integer> ignitedBlocks;

        FireStream(Vector3D direction, double range) {
            this.nextUpdate = 0;
            this.direction = direction.subtract(new Vector3D(0, direction.getY(), 0)).normalize();

            this.currentLocation = location.clone().add(this.direction);
            this.range = range;
            this.origin = location.clone();
            this.ignitedBlocks = new HashMap<>();
            this.blocked = false;
        }

        boolean update() {
            long time = System.currentTimeMillis();

            if (time < this.nextUpdate) return false;
            this.nextUpdate = time + INTERVAL;

            Iterator<Map.Entry<TempBlock, Integer>> iterator = ignitedBlocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TempBlock, Integer> entry = iterator.next();

                Integer timer = entry.getValue() - INTERVAL;

                if (timer <= 0) {
                    entry.getKey().reset();
                    iterator.remove();
                } else {
                    entry.setValue(timer);
                }
            }

            currentLocation = currentLocation.clone().add(direction);
            if (this.blocked || currentLocation.distanceSquared(origin) > range * range) {
                return this.ignitedBlocks.isEmpty();
            }

            Block block = currentLocation.getBlock();

            if (IgnitableBlocks.isIgnitable(block)) {
                ignite(block);
            } else if (IgnitableBlocks.isIgnitable(block.getRelative(BlockFace.DOWN))) {
                Block nextBlock = block.getRelative(BlockFace.DOWN);

                ignite(nextBlock);
                currentLocation = nextBlock.getLocation().clone();
            } else if (IgnitableBlocks.isIgnitable(block.getRelative(BlockFace.UP))) {
                Block nextBlock = block.getRelative(BlockFace.UP);

                ignite(nextBlock);
                currentLocation = nextBlock.getLocation().clone();
            } else {
                this.blocked = true;
            }

            return false;
        }

        void destroy() {
            for (TempBlock tempBlock : ignitedBlocks.keySet()) {
                tempBlock.reset();
            }
        }

        private void ignite(Block block) {
            BlockState state = block.getState();

            if (state.getType() == Material.FIRE) return;

            if (Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                TempBlock tempBlock = new TempBlock(state, Material.FIRE);
                ignitedBlocks.put(tempBlock, DURATION);
            } else {
                this.blocked = true;
            }
        }
    }

    private static class Config extends Configurable {
        public int range;

        @Override
        public void onConfigReload() {

        }
    }
}
