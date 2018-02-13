package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.BlockState;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class Blaze implements Ability {
    public static Config config = new Config();

    private User user;

    private Location location;
    private List<FireStream> fireStreams;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.location = user.getLocation();
        this.fireStreams = new ArrayList<>();
        this.user = user;

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

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
            Vector3D direction = VectorUtil.clearAxis(user.getDirection(), 1).normalize();
            direction = VectorUtil.rotate(direction, Vector3D.PLUS_J, angle).normalize();

            fireStreams.add(new FireStream(direction, config.range));
        }

        user.setCooldown(getDescription());
        return true;
    }

    @Override
    public UpdateResult update() {
        Iterator<FireStream> iterator = fireStreams.iterator();

        while (iterator.hasNext()) {
            FireStream fireStream = iterator.next();
            if (fireStream.update()) {
                iterator.remove();
                fireStream.destroy();
            }
        }

        return fireStreams.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        for (FireStream fireStream : fireStreams) {
            fireStream.destroy();
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Blaze";
    }

    @Override
    public void handleCollision(Collision collision) {

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

            if (!Game.getProtectionSystem().canBuild(user, currentLocation)) {
                return true;
            }

            Block block = currentLocation.getBlock();

            if (MaterialUtil.isIgnitable(block)) {
                ignite(block);
            } else if (MaterialUtil.isIgnitable(block.getRelative(BlockFace.DOWN))) {
                Block nextBlock = block.getRelative(BlockFace.DOWN);

                ignite(nextBlock);
                currentLocation = nextBlock.getLocation().clone();
            } else if (MaterialUtil.isIgnitable(block.getRelative(BlockFace.UP))) {
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
        public boolean enabled;
        public long cooldown;
        public double range;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "blaze");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            range = abilityNode.getNode("range").getDouble(7.0);
        }
    }
}
