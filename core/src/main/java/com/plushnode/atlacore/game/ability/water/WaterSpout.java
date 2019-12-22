package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
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
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.Levelled;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO: This is very similar to AirSpout. They should probably be merged eventually.
public class WaterSpout implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private AABB collider;
    private Flight flight;
    private List<Block> columnBlocks;
    private List<Block> spiralBlocks;
    private Block previousBlock;
    private double rotation;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (Game.getAbilityInstanceManager().destroyInstanceType(user, WaterSpout.class)) {
            return false;
        }

        WaterSpoutWave wave = new WaterSpoutWave();

        // Attempt to source a wave. If successful, cancel this WaterSpout.
        if (wave.activate(user, ActivationMethod.Punch)) {
            Game.getAbilityInstanceManager().addAbility(user, wave);
            return false;
        }

        Collection<WaterSpoutWave> waveInstances = Game.getAbilityInstanceManager().getPlayerInstances(user, WaterSpoutWave.class);

        // Don't allow users to spout while riding a wave.
        for (WaterSpoutWave waveInstance : waveInstances) {
            if (waveInstance.isActive()) {
                return false;
            }
        }

        if (user.getEyeLocation().getBlock().isLiquid()) {
            return false;
        }

        this.user = user;
        recalculateConfig();
        this.columnBlocks = new ArrayList<>();
        this.spiralBlocks = new ArrayList<>();
        this.rotation = 0.0;

        if (WorldUtil.distanceAboveGround(user, Material.WATER, Material.LAVA) > userConfig.height + userConfig.heightBuffer) {
            return false;
        }

        if (!isAboveBendableBlock()) {
            return false;
        }

        this.flight = Flight.get(user);
        this.flight.setFlying(true);

        // The only wave instances that are active are currently being sourced, so destroy them.
        for (WaterSpoutWave waveInstance : waveInstances) {
            Game.getAbilityInstanceManager().destroyInstance(user, waveInstance);
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        double maxHeight = userConfig.height + userConfig.heightBuffer;

        clearBlocks(spiralBlocks);

        if (!user.canBend(getDescription()) || !isAboveBendableBlock()) {
            return UpdateResult.Remove;
        }

        user.setSprinting(false);

        Location ground = RayCaster.cast(user.getWorld(), new Ray(user.getLocation(), Vector3D.MINUS_J), maxHeight + 1, true, columnBlocks);

        // Remove if player gets too far away from ground.
        if (ground.distanceSquared(user.getLocation()) > maxHeight * maxHeight) {
            return UpdateResult.Remove;
        }

        double distance = ground.distance(user.getLocation());

        // Remove flight when user goes above the top. This will drop them back down into the acceptable height.
        if (distance > userConfig.height) {
            flight.setFlying(false);

            // Push the user downwards since they are inside of water and gravity would be too slow.
            Vector3D velocity = user.getVelocity();
            velocity = velocity.add(new Vector3D(0, -0.1, 0));
            user.setVelocity(velocity);
        } else {
            flight.setFlying(true);
        }

        Location mid = ground.add(Vector3D.PLUS_J.scalarMultiply(distance / 2.0));

        // Create a bounding box for collision that extends through the spout from the ground to the player.
        collider = new AABB(new Vector3D(-0.5, -distance / 2.0, -0.5), new Vector3D(0.5, distance / 2.0, 0.5), user.getWorld()).at(mid);

        Block currentBlock = user.getLocation().getBlock();
        if (!currentBlock.equals(previousBlock)) {
            renderColumn(ground);
            previousBlock = currentBlock;
        }

        if (userConfig.spiralEnabled) {
            renderSpiral(ground);
        }

        return UpdateResult.Continue;
    }

    private void renderColumn(Location ground) {
        double dy = user.getLocation().getY() - ground.getY();

        clearBlocks(columnBlocks);

        for (int i = 0; i < dy; ++i) {
            Location location = ground.add(0, i, 0);

            // TODO: Waterlogged fence
            if (location.getBlock().getType() != Material.WATER) {
                new TempBlock(location.getBlock(), Material.WATER);
                columnBlocks.add(location.getBlock());
            }
        }
    }

    private void renderSpiral(Location ground) {
        double dy = user.getLocation().getY() - ground.getY() - userConfig.spiralBuffer;

        ground = ground.getBlock().getLocation().add(0.5, 0.5, 0.5);

        // Rotations need to be divisible by 45 so the angle lines up cleanly with the 8 surrounding blocks.
        double angle = Math.floor(this.rotation / 45.0) * 45.0;

        this.rotation += userConfig.spiralRotationSpeed;

        for (int i = 0; i < dy; ++i) {
            double x = Math.cos(Math.toRadians(angle));
            double y = Math.sin(Math.toRadians(angle));

            Location location = ground.add(x, i, y);

            if (MaterialUtil.isTransparent(location.getBlock())) {
                new TempBlock(location.getBlock(), Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(1)));
                spiralBlocks.add(location.getBlock());
            }

            angle += 360.0 / 8.0;
        }
    }

    private boolean isAboveBendableBlock() {
        double maxHeight = userConfig.height + userConfig.heightBuffer;
        Block groundBlock = RayCaster.blockCastIgnore(user.getWorld(), new Ray(user.getLocation(), Vector3D.MINUS_J), maxHeight + 1, true, columnBlocks);

        return groundBlock != null && (groundBlock.getType() == Material.WATER || groundBlock.getType() == Material.ICE);
    }

    private void clearBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            Game.getTempBlockService().reset(block);
        }

        blocks.clear();
    }

    @Override
    public void destroy() {
        clearBlocks(columnBlocks);
        clearBlocks(spiralBlocks);

        flight.setFlying(false);
        flight.release();
        user.setCooldown(this, userConfig.cooldown);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterSpout";
    }

    @Override
    public Collection<Collider> getColliders() {
        if (this.collider != null) {
            return Collections.singletonList(this.collider);
        }

        return Collections.emptyList();
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    // This modifies a user's velocity to cap it while on spout.
    public static void handleMovement(User user, Vector3D velocity) {
        List<WaterSpout> spouts = Game.getAbilityInstanceManager().getPlayerInstances(user, WaterSpout.class);

        if (spouts.isEmpty()) return;

        WaterSpout spout = spouts.get(0);

        // Don't consider y in the calculation.
        velocity = VectorUtil.clearAxis(velocity, 1);

        if (velocity.getNormSq() > spout.userConfig.maxSpeed * spout.userConfig.maxSpeed) {
            velocity = velocity.normalize().scalarMultiply(spout.userConfig.maxSpeed);
            user.setVelocity(velocity);
        }
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double height;
        public double heightBuffer;
        public double maxSpeed;
        public boolean spiralEnabled;
        public double spiralRotationSpeed;
        public double spiralBuffer;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterspout");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            height = abilityNode.getNode("height").getDouble(14.0);
            heightBuffer = abilityNode.getNode("height-buffer").getDouble(2.0);
            maxSpeed = abilityNode.getNode("max-speed").getDouble(0.2);
            spiralEnabled = abilityNode.getNode("spiral").getNode("enabled").getBoolean(true);
            spiralRotationSpeed = abilityNode.getNode("spiral").getNode("rotation-speed").getDouble(22.5);
            spiralBuffer = abilityNode.getNode("spiral").getNode("block-buffer").getDouble(2.0);
        }
    }
}
