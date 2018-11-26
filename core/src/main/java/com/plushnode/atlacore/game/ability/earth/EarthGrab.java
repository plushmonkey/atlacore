package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.Grid;
import com.plushnode.atlacore.game.ability.common.RisingColumn;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EarthGrab implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (user.isSneaking()) {
            TravelState newState = new TravelState();

            if (!newState.isValid()) {
                return false;
            }

            this.state = newState;
        } else {
            Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getLocation(), Vector3D.MINUS_J), 2.0, true);

            if (block == null || !MaterialUtil.isEarthbendable(block)) {
                return false;
            }

            this.state = new FormState(block.getLocation());
        }

        user.setCooldown(this, userConfig.cooldown);

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
        return "EarthGrab";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    private interface State {
        boolean update();
    }

    private class TravelState implements State {
        private Vector3D direction;
        private Location origin;
        private Location location;

        TravelState() {
            this.direction = user.getDirection();
            this.origin = user.getLocation();
            this.location = origin;

            if (this.direction.equals(Vector3D.PLUS_J) || this.direction.equals(Vector3D.MINUS_J)) {
                this.direction = null;
            } else {
                this.direction = VectorUtil.setY(this.direction, 0).normalize();
            }
        }

        public boolean isValid() {
            Location newLocation = RayCaster.cast(user, new Ray(location.add(0, 3.5, 0), Vector3D.MINUS_J), 7.0, true, false);

            if (!MaterialUtil.isEarthbendable(newLocation.getBlock().getRelative(BlockFace.DOWN))) {
                return false;
            }

            return this.direction != null;
        }

        @Override
        public boolean update() {
            // Push the location forward and climb/fall if necessary
            location = location.add(direction.scalarMultiply(userConfig.travelSpeed));
            if (!MaterialUtil.isTransparent(location.getBlock()) || MaterialUtil.isTransparent(location.getBlock().getRelative(BlockFace.DOWN))) {
                location = RayCaster.cast(user, new Ray(location.add(0, 3.5, 0), Vector3D.MINUS_J), 7.0, true, false);
            }

            if (!MaterialUtil.isEarthbendable(location.getBlock().getRelative(BlockFace.DOWN))) {
                return false;
            }

            if (location.distanceSquared(origin) > userConfig.travelRange * userConfig.travelRange) {
                return false;
            }

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            render();

            Sphere collider = new Sphere(location, userConfig.entityCollisionRadius);
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                Block block = RayCaster.blockCast(user.getWorld(), new Ray(entity.getLocation(), Vector3D.MINUS_J), 2.0, true);

                if (block == null || !MaterialUtil.isEarthbendable(block)) {
                    return false;
                }

                state = new FormState(block.getLocation());
                return true;
            }, true);

            return true;
        }

        private void render() {
            Material type = location.getBlock().getRelative(BlockFace.DOWN).getType();
            Game.plugin.getParticleRenderer().display(ParticleEffect.BLOCK_CRACK, 1.0f, 0.1f, 1.0f, 0.1f, 100, location, type);
        }
    }

    private class FormState implements State {
        private long nextUpdate;
        private List<RisingColumn> columns = new ArrayList<>();

        FormState(Location center) {
            center = RayCaster.cast(user, new Ray(center, Vector3D.MINUS_J), 2.0, true, false);
            this.nextUpdate = System.currentTimeMillis();

            // Construct a grid with a circle painted that represents the surrounding dome.
            Grid grid = new Grid(userConfig.radius * 2 + 10) {
                @Override
                public void draw() {
                    drawCircle(0, 0, userConfig.radius, userConfig.radius + 1, 2);
                    drawCircle(0, 0, userConfig.radius, userConfig.radius, 1);
                }
            };

            grid.draw();

            Set<Block> columnBases = new HashSet<>();

            int lookup = (int)Math.floor(grid.getGrid().length / 2.0) - 1;
            for (int x = -lookup; x < lookup; ++x) {
                for (int z = -lookup; z < lookup; ++z) {
                    if (grid.getValue(x, z) != 0) {
                        Location location = center.add(x, 0, z).getBlock().getLocation();
                        int height = (int)Math.ceil(userConfig.height / (double)grid.getValue(x, z));

                        location = getGround(location, (int)Math.floor(height / 2.0));

                        Block block = location.getBlock();
                        if (MaterialUtil.isEarthbendable(block) && MaterialUtil.isTransparent(block.getRelative(BlockFace.UP))) {
                            if (!columnBases.contains(block)) {
                                RisingColumn column = new RisingColumn(user, block, height, userConfig.duration, false);
                                columns.add(column);
                                columnBases.add(block);
                            }
                        }
                    }
                }
            }
        }

        private Location getGround(Location location, int wiggle) {
            if (MaterialUtil.isTransparent(location.getBlock())) {
                Block block = RayCaster.blockCast(user.getWorld(), new Ray(location, Vector3D.MINUS_J), wiggle, true);

                if (block != null) {
                    location = block.getLocation();
                }
            }

            if (!MaterialUtil.isTransparent(location.getBlock().getRelative(BlockFace.UP))) {
                for (int i = 1; i <= wiggle; ++i) {
                    Block check = location.getBlock().getRelative(BlockFace.UP, i);
                    if (MaterialUtil.isTransparent(check)) {
                        return check.getRelative(BlockFace.DOWN).getLocation();
                    }
                }
            }

            return location;
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();

            // Allow it to update multiple times within one tick to match raise interval.
            while (time > this.nextUpdate && !columns.isEmpty()) {
                this.nextUpdate += userConfig.raiseInterval;

                columns.removeIf(RisingColumn::update);
            }

            return !columns.isEmpty();
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.HEIGHT)
        public int height;
        @Attribute(Attributes.RADIUS)
        public int radius;
        @Attribute(Attributes.SPEED)
        public int raiseInterval;
        @Attribute(Attributes.DURATION)
        public int duration;
        @Attribute(Attributes.SPEED)
        public double travelSpeed;
        @Attribute(Attributes.RANGE)
        public double travelRange;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "earthgrab");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            height = abilityNode.getNode("height").getInt(4);
            radius = abilityNode.getNode("radius").getInt(4);
            duration = abilityNode.getNode("duration").getInt(10000);
            raiseInterval = abilityNode.getNode("raise-interval").getInt(80);
            travelSpeed = abilityNode.getNode("travel-speed").getDouble(1.0);
            travelRange = abilityNode.getNode("travel-range").getDouble(10.0);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(2.5);
        }
    }
}
