package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.*;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class SurgeWall implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state = null;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (user.isOnCooldown(getDescription())) {
            return false;
        }

        if (method == ActivationMethod.Punch) {
            if (this.state == null || this.state instanceof SourceState) {

                List<Material> materials = new ArrayList<>(Arrays.asList(MaterialUtil.getPlantMaterials()));
                materials.add(Material.WATER);

                Block newSource = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.selectRange, materials);

                if (newSource == null || (newSource.getType() != Material.WATER && !MaterialUtil.isPlant(newSource.getType()))) {
                    return false;
                }

                this.state = new SourceState(newSource);
            } else {
                this.state.onPunch();
            }
        } else if (method == ActivationMethod.Sneak) {
            if (this.state != null) {
                this.state.onSneak();
            }
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        if (!state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
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
        return "SurgeWall";
    }

    @Override
    public Collection<Collider> getColliders() {
        Collider collider= state.getCollider();

        if (collider == null) {
            return null;
        }

        return Collections.singletonList(collider);
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    private interface State {
        boolean update();
        void onPunch();
        void onSneak();
        Collider getCollider();
    }

    private class SourceState implements State {
        private Block sourceBlock;
        private long startTime;

        SourceState(Block source) {
            this.startTime = System.currentTimeMillis();
            this.sourceBlock = source;
        }

        @Override
        public boolean update() {
            if (user.getLocation().distanceSquared(sourceBlock.getLocation()) > userConfig.selectMaxDistance * userConfig.selectMaxDistance) {
                return false;
            }

            Location renderLocation = this.sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return System.currentTimeMillis() < startTime + userConfig.selectTimeout;
        }

        @Override
        public void onPunch() {

        }

        @Override
        public void onSneak() {
            state = new TravelState(sourceBlock.getLocation());
        }

        @Override
        public Collider getCollider() {
            return null;
        }
    }

    private class TravelState implements State {
        private Location location;
        private TempBlock tempBlock;

        TravelState(Location origin) {
            this.location = origin;
            this.tempBlock = null;
        }

        @Override
        public boolean update() {
            Location target = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.extension));

            if (this.location.distanceSquared(target) <= userConfig.travelSpeed) {
                state = new WallState();
                if (this.tempBlock != null) {
                    this.tempBlock.reset();
                    this.tempBlock = null;
                }
                return true;
            }

            Vector3D direction = target.subtract(this.location).toVector().normalize();

            this.location = this.location.add(direction.scalarMultiply(userConfig.travelSpeed));

            if (this.tempBlock != null) {
                this.tempBlock.reset();
                this.tempBlock = null;
            }

            if (MaterialUtil.isTransparent(this.location.getBlock())) {
                tempBlock = new TempBlock(this.location.getBlock(), Material.WATER);
            }

            return true;
        }

        @Override
        public void onPunch() {

        }

        @Override
        public void onSneak() {

        }

        @Override
        public Collider getCollider() {
            return null;
        }
    }

    private class WallState implements State {
        private Location location;
        private Disc disc;
        private List<TempBlock> tempBlocks = new ArrayList<>();
        private Material type;
        private long nextToggle;

        WallState() {
            this.type = Material.WATER;
            this.nextToggle = 0;
        }

        @Override
        public boolean update() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();

            double dist = RayCaster.cast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.extension, false).distance(user.getEyeLocation());

            updateDisc(dist);

            List<Block> blocks = new ArrayList<>();

            for (Block block : WorldUtil.getNearbyBlocks(location, userConfig.radius + 1)) {
                if (MaterialUtil.isTransparent(block)) {
                    if (disc.intersects(AABB.BLOCK_BOUNDS.at(block.getLocation()))) {
                        // Check line of sight to the target block if it's not the center one.
                        if (!block.equals(location.getBlock())) {
                            Location target = block.getLocation().add(0.5, 0.5, 0.5);
                            Vector3D toTarget = target.subtract(location).toVector();
                            Vector3D direction = toTarget.normalize();
                            double distance = toTarget.getNorm();

                            double distSq = RayCaster.cast(user.getWorld(), new Ray(location, direction), distance, false).distanceSquared(location);

                            if (Math.abs(distSq - (distance * distance)) > 0.01) {
                                continue;
                            }
                        }

                        // Only add blocks to the wall if they aren't intersecting the user.
                        if (!AABB.BLOCK_BOUNDS.at(block.getLocation()).intersects(user.getBounds().at(user.getLocation()))) {
                            blocks.add(block);
                        }
                    }
                }
            }

            for (Block block : blocks) {
                tempBlocks.add(new TempBlock(block, type));
            }

            if (!user.isSneaking()) {
                tempBlocks.forEach(TempBlock::reset);
                user.setCooldown(SurgeWall.this, userConfig.cooldown);
                return false;
            }

            return true;
        }

        @Override
        public void onPunch() {
            long time = System.currentTimeMillis();

            if (time >= this.nextToggle) {
                this.type = type == Material.WATER ? Material.ICE : Material.WATER;
                this.nextToggle = time + 100;
            }
        }

        @Override
        public void onSneak() {

        }

        @Override
        public Collider getCollider() {
            return disc;
        }

        private void updateDisc(double extension) {
            final double r = userConfig.radius;
            final double ht = 0.25;

            this.location = user.getEyeLocation().add(user.getDirection().scalarMultiply(extension));

            AABB aabb = new AABB(new Vector3D(-r, -r, -ht), new Vector3D(r, r, ht));
            Vector3D right = VectorUtil.normalizeOrElse(user.getDirection().crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
            Rotation rot = new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw()));
            rot = rot.applyTo(new Rotation(right, Math.toRadians(user.getPitch())));

            this.disc = new Disc(new OBB(aabb, rot, user.getWorld()).at(location), new Sphere(location, r));
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.SELECTION)
        public double selectMaxDistance;
        @Attribute(Attributes.SELECTION)
        public long selectTimeout;
        @Attribute(Attributes.RADIUS)
        public double radius;
        public double extension;
        @Attribute(Attributes.SPEED)
        public double travelSpeed;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "surge", "wall");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            selectRange = abilityNode.getNode("select-range").getDouble(10.0);
            selectMaxDistance = abilityNode.getNode("select-max-distance").getDouble(25.0);
            selectTimeout = abilityNode.getNode("select-timeout").getLong(30000);
            radius = abilityNode.getNode("radius").getDouble(2.0);
            extension = abilityNode.getNode("extension").getDouble(5.0);
            travelSpeed = abilityNode.getNode("travel-speed").getDouble(3.0);
        }
    }
}
