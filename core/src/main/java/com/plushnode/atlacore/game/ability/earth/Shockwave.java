package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.Grid;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.policies.removal.OutOfWorldRemovalPolicy;
import com.plushnode.atlacore.protection.ProtectionSystem;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.block.TempBlockService;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class Shockwave implements Ability {
    public static Config config = new Config();
    private static List<Shockwave> instances = new ArrayList<>();

    private User user;
    private Config userConfig;
    private boolean charged;
    private Location origin;
    private boolean released;
    private int[][] offsets;
    private int[][] obstructions;
    private ShockwaveGrid shockwaveGrid;
    private Map<Block, TempBlock> tempBlocks;
    private int distance;
    private boolean conal;
    private long lastUpdate;
    private Vector3D coneDirection = null;
    // Used to only damage entities once. They can be knocked back multiple times.
    private List<Entity> affectedEntities;
    private long startTime;

    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.startTime = System.currentTimeMillis();

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        if (method == ActivationMethod.Fall) {
            // Don't activate fall method when user is sneaking.
            if (user instanceof Player && ((Player) user).isSneaking()) {
                return false;
            }

            // Make sure the user fall distance is high enough.
            if (!canFallActivate(user)) {
                return false;
            }
        } else if (method == ActivationMethod.Punch) {
            for (Shockwave instance : instances) {
                if (instance.user == user && !instance.isReleased()) {
                    instance.activateConal();
                    break;
                }
            }

            return false;
        }

        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.charged = false;
        this.released = false;
        this.offsets = new int[userConfig.range * 2 + 1][userConfig.range * 2 + 1];
        this.obstructions = new int[userConfig.range * 2 + 1][userConfig.range * 2 + 1];
        this.distance = 2;
        this.shockwaveGrid = new ShockwaveGrid();
        this.tempBlocks = new HashMap<>();
        this.affectedEntities = new ArrayList<>();
        this.conal = false;
        this.lastUpdate = 0;
        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsOfflineRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user)
        );

        instances.add(this);

        if (method == ActivationMethod.Fall) {
            skipCharging();
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        long time = System.currentTimeMillis();

        if (!this.charged) {
            this.charged = time >= startTime + userConfig.chargeTime;
        }

        if (isCharging()) {
            return user.isSneaking() ? UpdateResult.Continue : UpdateResult.Remove;
        }

        if (!this.released) {
            if (!user.isSneaking() || this.conal) {
                this.released = true;
                this.origin = user.getLocation().clone();

                user.setCooldown(getDescription(), userConfig.cooldown);

                buildOffsets();

                Block originBlock = this.origin.getBlock().getRelative(BlockFace.DOWN);

                ProtectionSystem ps = Game.getProtectionSystem();
                if (!ps.canBuild(user, originBlock.getLocation()) || !ps.canBuild(user, originBlock.getRelative(BlockFace.DOWN).getLocation())) {
                    return UpdateResult.Remove;
                }
            } else {
                Vector3D direction = user.getDirection();
                Location location = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                location = location.add(side.scalarMultiply(0.5));

                Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
            }
        } else {
            if (time > this.lastUpdate) {
                if (updateReleased()) {
                    return UpdateResult.Remove;
                }

                this.lastUpdate = time;
            }
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        TempBlockService tbm = Game.getTempBlockService();

        for (Block block : tempBlocks.keySet()) {
            TempBlock tempBlock = tbm.getTempBlock(block);

            if (tempBlock != null) {
                tbm.reset(tempBlock);
            }
        }

        instances.remove(this);
    }

    private void buildOffsets() {
        Location blockLocation = origin.clone();

        double averageY = origin.getY();
        int count = 1;
        final double allowedDeviation = 7;

        for (int x = -userConfig.range; x < userConfig.range; ++x) {
            for (int z = -userConfig.range; z < userConfig.range; ++z) {

                blockLocation = blockLocation.setX(origin.getX() + x);
                blockLocation = blockLocation.setY(origin.getY());
                blockLocation = blockLocation.setZ(origin.getZ() + z);

                int y = origin.getBlockY();

                boolean down = MaterialUtil.isTransparent(blockLocation.getBlock()) || isShockwaveLocation(blockLocation.getBlock());
                while (true) {
                    if (y <= 0 || y >= 256) break;

                    blockLocation = blockLocation.setY(y);

                    if (down) {
                        if (!MaterialUtil.isTransparent(blockLocation.getBlock().getRelative(BlockFace.DOWN)) && !isShockwaveLocation(blockLocation.getBlock()))
                            break;
                        --y;
                    } else {
                        if (MaterialUtil.isTransparent(blockLocation.getBlock()) || isShockwaveLocation(blockLocation.getBlock()))
                            break;
                        ++y;
                    }
                }

                averageY = (averageY * count + (double)y) / (count + 1);

                int yOffset = y - origin.getBlockY();
                int averageOffset = (int)(averageY - origin.getY());

                if (Math.abs(yOffset - averageOffset) > allowedDeviation) {
                    this.obstructions[userConfig.range + x][userConfig.range + z] = 1;
                } else {
                    this.offsets[userConfig.range + x][userConfig.range + z] = yOffset;
                    this.obstructions[userConfig.range + x][userConfig.range + z] = 0;
                }
            }
        }

        updateObstructions();
    }

    public boolean isShockwaveLocation(Block block) {
        for (Shockwave sw : instances) {
            if (sw.tempBlocks.containsKey(block)) {
                return true;
            }
        }
        return false;
    }

    private void updateObstructions() {
        double delta = 360.0 / (2 * Math.PI * userConfig.range) - 1;

        for (double theta = 0.0; theta < 360; theta += delta) {
            double rads = Math.toRadians(theta);

            Vector3D direction = new Vector3D(Math.sin(rads), 0, Math.cos(rads));

            Vector3D current = new Vector3D(0, 0, 0);
            Vector3D previous = Vector3D.ZERO;

            for (int i = 0; i < userConfig.range; ++i) {
                current = current.add(direction);

                int x = (int)Math.floor(current.getX());
                int z = (int)Math.floor(current.getZ());
                int prevX = (int)Math.floor(previous.getX());
                int prevZ = (int)Math.floor(previous.getZ());

                int offset = this.offsets[userConfig.range + x][userConfig.range + z];
                int prevOffset = this.offsets[userConfig.range + prevX][userConfig.range + prevZ];

                if (Math.abs(offset - prevOffset) > 2 || this.obstructions[userConfig.range + x][userConfig.range + z] == 1) {
                    this.obstructions[userConfig.range + x][userConfig.range + z] = 1;
                    markObstructed(current, direction);
                    break;
                }

                previous = current;
            }
        }
    }

    private void markObstructed(Vector3D obstruction, Vector3D direction) {
        Vector3D obstructed = obstruction;

        int obsX = (int)Math.floor(obstructed.getX());
        int obsZ = (int)Math.floor(obstructed.getZ());
        this.obstructions[userConfig.range + obsX][userConfig.range + obsZ] = 1;

        while (true) {
            obstructed = obstructed.add(direction);

            int x = (int)Math.floor(obstructed.getX());
            int z = (int)Math.floor(obstructed.getZ());

            if (Math.abs(x) > userConfig.range) break;
            if (Math.abs(z) > userConfig.range) break;

            this.obstructions[userConfig.range + x][userConfig.range + z] = 1;
        }
    }

    private void updateTempBlocks() {
        Iterator<Map.Entry<Block, TempBlock>> iterator = tempBlocks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Block, TempBlock> entry = iterator.next();
            Block block = entry.getKey();
            Vector3D vector = VectorUtil.getBlockVector(block.getLocation().toVector());

            int gridX = (int)vector.getX() - this.origin.getBlockX();
            int gridY = (int)vector.getZ() - this.origin.getBlockZ();

            int value = shockwaveGrid.getValue(gridX, gridY);

            TempBlockService tbm = Game.getTempBlockService();

            if (value == 0) {
                TempBlock tempBlock = tbm.getTempBlock(block);

                if (tempBlock != null) {
                    tbm.reset(tempBlock);
                }

                iterator.remove();
            } else if (value == 1) {
                Block below = block.getRelative(BlockFace.DOWN);

                if (this.tempBlocks.containsKey(below)) {
                    TempBlock tempBlock = tbm.getTempBlock(block);

                    if (tempBlock != null) {
                        tbm.reset(tempBlock);
                    }

                    iterator.remove();
                }
            }
        }
    }

    private boolean updateReleased() {
        distance += 1;
        shockwaveGrid.update();

        Location blockLocation = this.origin.clone();

        for (int x = -userConfig.range; x < userConfig.range; ++x) {
            for (int z = -userConfig.range; z < userConfig.range; ++z) {
                if (this.obstructions[x + userConfig.range][z + userConfig.range] == 1) continue;

                int value = shockwaveGrid.getValue(x, z);
                if (value == 0) continue;

                blockLocation = blockLocation.setX(this.origin.getBlockX() + x);
                blockLocation = blockLocation.setY(this.origin.getBlockY() + this.offsets[x + userConfig.range][z + userConfig.range]);
                blockLocation = blockLocation.setZ(this.origin.getBlockZ() + z);

                if (this.conal) {
                    Vector3D coneCheck = new Vector3D(x, 0, z);

                    if (Vector3D.angle(coneCheck, this.coneDirection) > Math.toRadians(userConfig.angle)) {
                        continue;
                    }
                }

                Material material = blockLocation.getBlock().getRelative(BlockFace.DOWN).getType();
                Block block = blockLocation.getBlock();

                if (!MaterialUtil.isTransparent(blockLocation.getBlock()) || blockLocation.getBlock().isLiquid() || !MaterialUtil.isEarthbendable(blockLocation.getBlock().getRelative(BlockFace.DOWN))) {
                    if (!isShockwaveLocation(block)) {
                        markObstructed(new Vector3D(x, 0, z), new Vector3D(x, 0, z).normalize());
                        updateObstructions();
                        continue;
                    }
                }

                if (!Game.getProtectionSystem().canBuild(user, blockLocation)) {
                    markObstructed(new Vector3D(x, 0, z), new Vector3D(x, 0, z).normalize());
                    updateObstructions();
                    continue;
                }

                if (material == Material.SAND) {
                    material = Material.SANDSTONE;
                } else if (material == Material.GRAVEL) {
                    material = Material.COBBLESTONE;
                }

                TempBlockService tbm = Game.getTempBlockService();
                for (int i = 0; i < shockwaveGrid.getValue(x, z); ++i) {
                    TempBlock tempBlock = tbm.getTempBlock(block);

                    if (tempBlock == null) {
                        tempBlock = new TempBlock(block, material, false);
                        tbm.add(tempBlock);
                        tempBlocks.put(block, tempBlock);

                        Vector3D direction = new Vector3D(x, 0, z).normalize();

                        hitEntities(block.getLocation(), direction, 2);
                    }

                    block = block.getRelative(BlockFace.UP);
                }
            }
        }

        updateTempBlocks();

        return distance >= userConfig.range;
    }

    private void hitEntities(Location location, Vector3D direction, double radius) {
        List<Entity> nearbyEntities = getNearbyEntities(location, radius);

        direction = new Vector3D(direction.getX(), 0.5, direction.getZ());
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.equals(user)) continue;

            if (!affectedEntities.contains(entity)) {
                ((LivingEntity) entity).damage(userConfig.damage);
                affectedEntities.add(entity);
            }

            entity.setVelocity(direction.scalarMultiply(userConfig.knockback));
        }
    }

    public static List<Entity> getNearbyEntities(Location location, double radius) {
        List<Entity> nearbyEntities = new ArrayList<>();
        double radiusSq = radius * radius;
        int radiusInt = (int)radius;

        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, radiusInt, radiusInt, radiusInt);

        for (Entity entity : entities) {
            if (entity.getLocation().distanceSquared(location) <= radiusSq) {
                nearbyEntities.add(entity);
            }

        }
        return nearbyEntities;
    }

    public boolean isCharging() {
        return !this.charged;
    }

    public void skipCharging() {
        this.charged = true;
    }

    public boolean isReleased() {
        return this.released;
    }

    public static boolean canFallActivate(User user) {
        return user.getFallDistance() >= config.fallThreshold;
    }

    public void activateConal() {
        if (!isCharging()) {
            this.conal = true;

            this.coneDirection = VectorUtil.setY(user.getDirection(), 0);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Shockwave";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    class ShockwaveGrid extends Grid {
        public ShockwaveGrid() {
            super(userConfig.range * 2 + 2);
        }

        @Override
        public void draw() {
            drawCircle(0, 0, distance-2, distance, 1);
            drawCircle(0, 0, distance-1, distance-1, 2);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public double fallThreshold;
        @Attribute(Attributes.CHARGE_TIME)
        public long chargeTime;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.STRENGTH)
        public double knockback;
        @Attribute(Attributes.RANGE)
        public int range;
        public int angle;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "shockwave");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            fallThreshold = abilityNode.getNode("fall-threshold").getDouble(12.0);
            chargeTime = abilityNode.getNode("charge-time").getLong(2500);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            damage = abilityNode.getNode("damage").getDouble(4.0);
            knockback = abilityNode.getNode("knockback").getDouble(1.1);
            range = abilityNode.getNode("range").getInt(15);
            angle = abilityNode.getNode("angle").getInt(40);


            abilityNode.getNode("fall-threshold").setComment("How far a player needs to fall to activate Shockwave.");
            abilityNode.getNode("charge-time").setComment("How many milliseconds a player needs to charge before Shockwave can be released.");
            abilityNode.getNode("knockback").setComment("How strong the knockback is when an entity is hit by Shockwave.");
            abilityNode.getNode("range").setComment("How far away should the Shockwave travel.");
            abilityNode.getNode("angle").setComment("The angle for the punch-activated version.");
        }
    }
}
