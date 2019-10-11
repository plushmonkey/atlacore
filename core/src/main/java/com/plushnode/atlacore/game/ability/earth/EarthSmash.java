package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
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
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.CannotBendRemovalPolicy;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
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
import java.util.stream.Collectors;

public class EarthSmash implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Boulder boulder;
    private State state;
    private int tick;
    private long startTime;
    private List<Block> initialBlocks = new ArrayList<>();
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.tick = 0;
        this.startTime = System.currentTimeMillis();

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        if (method == ActivationMethod.Sneak) {
            List<EarthSmash> earthSmashes = Game.getAbilityInstanceManager().getPlayerInstances(user, EarthSmash.class);
            if (!earthSmashes.isEmpty()) {
                EarthSmash eSmash = earthSmashes.get(0);

                if (eSmash.userConfig == null) return false;

                double halfSize = eSmash.boulder.getSize() / 2.0;
                Location center = eSmash.boulder.getBaseBlockLocation().add(halfSize, halfSize, halfSize);

                if (eSmash.userConfig.flyEnabled && (!WorldUtil.isOnGround(user) || user.getLocation().getY() > center.getY())) {
                    double halfExtent = halfSize + 0.25;

                    Vector3D min = new Vector3D(-halfExtent, 0, -halfExtent);
                    Vector3D max = new Vector3D(halfExtent, eSmash.userConfig.flyBoundsSize, halfExtent);

                    // Create a bounding box that encompasses the upper half of the boulder with some extra space around it for activating flight.
                    AABB bounds = new AABB(min, max, center.getWorld()).at(center);

                    if (bounds.intersects(user.getBounds().at(user.getLocation()))) {
                        eSmash.enterFlyState();
                        return false;
                    }
                }

                if (eSmash.userConfig.grabEnabled) {
                    Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), eSmash.userConfig.grabRange, true);

                    if (block != null) {
                        if (eSmash.isBoulderBlock(block)) {
                            double distance = Math.min(center.distance(user.getEyeLocation()), eSmash.userConfig.grabRange);
                            eSmash.enterHoldState(distance);
                        }
                    }
                }

                return false;
            }
        } else if (method == ActivationMethod.Punch || method == ActivationMethod.Use) {
            List<EarthSmash> earthSmashes = Game.getAbilityInstanceManager().getPlayerInstances(user, EarthSmash.class);
            if (!earthSmashes.isEmpty()) {
                EarthSmash eSmash = earthSmashes.get(0);

                Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), eSmash.userConfig.grabRange, true);
                if (block != null && eSmash.isBoulderBlock(block)) {
                    if (eSmash.state instanceof HoldState) {
                        if (method == ActivationMethod.Punch) {
                            eSmash.enterTravelState();
                        } else {
                            int size = eSmash.boulder.getSize();

                            user.teleport(eSmash.boulder.getBaseBlockLocation().add(size / 2.0, size - 1, size/2.0));
                            eSmash.enterFlyState();
                        }
                    }
                }
            }

            return false;
        } else {
            return false;
        }

        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.state = new ChargeState();

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new CannotBendRemovalPolicy(user, getDescription(), true, true),
                new IsOfflineRemovalPolicy(user)
        );

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (time > startTime + userConfig.maxDuration || this.removalPolicy.shouldRemove()) {
            return remove();
        }

        if (!state.update()) {
            return remove();
        }

        return UpdateResult.Continue;
    }

    private UpdateResult remove() {
        if (boulder != null) {
            for (int i = 0; i < boulder.getSize(); ++i) {
                Layer layer = boulder.getLayer(i);

                for (int y = 0; y < layer.getSize(); ++y) {
                    for (int x = 0; x < layer.getSize(); ++x) {
                        Block block = boulder.getBase().add(x, i, y).getBlock();
                        Game.getTempBlockService().reset(block);
                    }
                }
            }

            for (Block initialBlock : initialBlocks) {
                Game.getTempBlockService().reset(initialBlock);
            }
            initialBlocks.clear();
        }

        return UpdateResult.Remove;
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
        return "EarthSmash";
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.emptyList();
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public boolean isBoulderBlock(Block block) {
        double sizeBounds = boulder.getSize() + 1;

        Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        Location baseCenter = boulder.getBase().add(0.5, 0.5, 0.5);

        if (blockCenter.distanceSquared(baseCenter) > sizeBounds * sizeBounds) {
            return false;
        }

        for (int i = 0; i < boulder.getSize(); ++i) {
            for (int y = 0; y < boulder.getSize(); ++y) {
                for (int x = 0; x < boulder.getSize(); ++x) {
                    Block check = boulder.getBase().add(x, i, y).getBlock();

                    if (block.equals(check)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void enterHoldState(double distance) {
        this.state = new HoldState(distance);
    }

    public void enterTravelState() {
        this.state = new TravelState();
    }

    public void enterFlyState() {
        this.state = new FlyState();
    }

    private interface State {
        boolean update();
    }

    private class ChargeState implements State {
        private long chargeStartTime;

        ChargeState() {
            chargeStartTime = System.currentTimeMillis();
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();

            // Refresh the global start timer so it doesn't time out while charging.
            startTime = time;

            if (time >= chargeStartTime + userConfig.chargeTime) {
                Vector3D direction = user.getDirection();
                Location location = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                location = location.add(side.scalarMultiply(0.5));

                Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, location);

                if (!user.isSneaking()) {
                    Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.selectRange, true);
                    if (block == null) {
                        return false;
                    }

                    if (!MaterialUtil.isEarthbendable(block)) {
                        return false;
                    }

                    if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                        return false;
                    }

                    boulder = new Boulder(block);
                    state = new RaiseState();

                    return isValidInitialBoulder();
                }
                return true;
            }

            return user.isSneaking();
        }

        private boolean isValidInitialBoulder() {
            int count = 0;
            for (int y = 0; y < boulder.getSize(); ++y) {
                for (int x = 0; x < boulder.getSize(); ++x) {
                    if (boulder.isValidColumn(x, y)) {
                        ++count;
                    }
                }
            }

            return count >= userConfig.minColumns;
        }
    }

    // Used for any state that allows the user to control the boulder.
    private abstract class ControlState implements State {
        public abstract boolean updateState();
        protected abstract boolean removeOnCollision();

        @Override
        public boolean update() {
            Location prevBase = boulder.getBase();
            List<Layer> prevBoulderState = boulder.getState();

            resetPreviousBoulder(prevBase);

            if (!updateState()) {
                return false;
            }

            // Update the boulder before checking if it's a valid base.
            // That allows surrounding terrain to reshape the boulder and allow it to pass.
            boulder.update();

            List<Layer> currentBoulderState = boulder.getState();

            if (tick > boulder.getSize() && !isValidBase(boulder.getBase())) {
                if (removeOnCollision()) {
                    return false;
                }

                boulder.setBase(prevBase);
            }

            renderBoulder(currentBoulderState);
            clearRaiseArea(prevBase, prevBoulderState);

            return true;
        }

        private void renderBoulder(List<Layer> currentBoulderState) {
            if (!boulder.canRender()) {
                return;
            }

            for (int y = 0; y < boulder.getSize(); ++y) {
                for (int x = 0; x < boulder.getSize(); ++x) {
                    if (boulder.isValidColumn(x, y)) {
                        for (int i = 0; i < boulder.getSize(); ++i) {
                            Layer layer = currentBoulderState.get(i);

                            Material type = layer.getState(x, y);
                            Location current = boulder.getBase().add(x, i, y);

                            if (type != Material.AIR && current.getBlock().getType() != type) {
                                new TempBlock(current.getBlock(), type, 10000);
                            }
                        }
                    }
                }
            }
        }

        private void clearRaiseArea(Location prevBase, List<Layer> prevBoulderState) {
            if (!prevBase.equals(boulder.getBase())) {
                if (tick < userConfig.radius && tick > 0) {
                    Layer layer = prevBoulderState.get(userConfig.radius / 2);

                    for (int y = 0; y < boulder.getSize(); ++y) {
                        for (int x = 0; x < boulder.getSize(); ++x) {
                            if ((tick > 1 || layer.getState(x, y) == Material.AIR) && boulder.isValidColumn(x, y)) {
                                Location current = boulder.getBase().add(x, -1, y);

                                if (MaterialUtil.isEarthbendable(current.getBlock())) {
                                    initialBlocks.add(current.getBlock());
                                    new TempBlock(current.getBlock(), Material.AIR);
                                }
                            }
                        }
                    }
                }

                ++tick;
            }
        }

        private void resetPreviousBoulder(Location prevBase) {
            for (int i = 0; i < boulder.getSize(); ++i) {
                for (int y = 0; y < boulder.getSize(); ++y) {
                    for (int x = 0; x < boulder.getSize(); ++x) {
                        Block block = prevBase.add(x, i, y).getBlock();
                        Material type = block.getType();

                        // Update the boulder so any changed blocks are reflected in the boulder state.
                        if (boulder.canRender() && type != boulder.getLayer(i).getState(x, y)) {
                            if (!MaterialUtil.isEarthbendable(type)) {
                                boulder.getLayer(i).setState(x, y, Material.AIR);
                            }
                        }

                        if (initialBlocks.contains(block)) {
                            new TempBlock(block, Material.AIR);
                        } else {
                            Game.getTempBlockService().reset(block);
                        }

                        // Don't immediately reset plant blocks since the block below could be air.
                        if (MaterialUtil.isTransparent(block) && !MaterialUtil.isAir(block.getType())) {
                            new TempBlock(block, Material.AIR, 10000);
                        }
                    }
                }
            }
        }

        protected boolean isValidBase(Location base) {
            for (int i = 0; i < boulder.getSize(); ++i) {
                Layer layer = boulder.getLayer(i);

                for (int y = 0; y < boulder.getSize(); ++y) {
                    for (int x = 0; x < boulder.getSize(); ++x) {
                        if (layer.getState(x, y) != Material.AIR) {
                            Location check = base.add(x, i, y);

                            if (!MaterialUtil.isTransparent(check.getBlock())) {
                                return false;
                            }

                            if (!Game.getProtectionSystem().canBuild(user, check)) {
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }
    }

    // This state is active after charging. It raises the boulder up from the ground into its idle position.
    private class RaiseState extends ControlState {
        private Location targetBase;
        private long nextRaiseTime;

        RaiseState() {
            targetBase = boulder.getBase().add(0, boulder.getSize() + 1, 0);
            this.nextRaiseTime = 0;
        }

        @Override
        public boolean updateState() {
            if (boulder.getBase().getY() >= targetBase.getY()) {
                state = new IdleState();
                return true;
            }

            long time = System.currentTimeMillis();

            if (time >= nextRaiseTime) {
                AABB bounds = boulder.getBoundingBox().at(boulder.getBaseBlockLocation());

                CollisionUtil.handleEntityCollisions(user, bounds, (entity) -> {
                    Vector3D vel = entity.getVelocity();
                    // Preserve horizontal movement while setting vertical velocity.
                    // This isn't accumulated so the player doesn't fly high into the air.
                    Vector3D newVelocity = new Vector3D(vel.getX(), userConfig.raiseEntityPush, vel.getZ());

                    entity.setVelocity(newVelocity);
                    return false;
                }, true, true);

                boulder.setBase(boulder.getBase().add(0, 1, 0));
                nextRaiseTime = time + 50;
            }

            return true;
        }

        @Override
        protected boolean removeOnCollision() {
            return false;
        }
    }

    private class IdleState extends ControlState {
        @Override
        public boolean updateState() {
            return true;
        }

        @Override
        protected boolean removeOnCollision() {
            return false;
        }
    }

    // This state is active when the player is holding sneak and using it as a shield.
    private class HoldState extends ControlState {
        private double grabDistance;

        HoldState(double distance) {
            this.grabDistance = Math.max(distance, boulder.getSize() + 1);
        }

        @Override
        public boolean updateState() {
            if (!user.isSneaking()) {
                state = new IdleState();
                return true;
            }

            int halfSize = (int)(boulder.getSize() / 2.0);
            Location targetCenter = RayCaster.cast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), grabDistance, true, boulder.getBlocks());
            Location newBase = targetCenter.subtract(halfSize, halfSize, halfSize);

            // Attempt to place the boulder as far away as possible while avoiding collisions.
            for (int i = 0; i < (int)targetCenter.distance(user.getEyeLocation()); ++i) {
                Location check = newBase.subtract(user.getDirection().scalarMultiply(i));

                if (isValidBase(check)) {
                    boulder.setBase(check);
                    return true;
                }
            }

            return true;
        }

        @Override
        protected boolean removeOnCollision() {
            return false;
        }
    }

    private class FlyState extends ControlState {
        private Flight flight;
        private long startTime;

        FlyState() {
            flight = Flight.get(user);
            flight.setFlying(true);
            startTime = System.currentTimeMillis();
        }

        @Override
        public boolean updateState() {
            if (!user.isSneaking() || System.currentTimeMillis() > startTime + userConfig.flyDuration) {
                state = null;
                flight.setFlying(false);
                flight.release();
                user.setCooldown(getDescription(), userConfig.cooldown);
                return false;
            }

            Vector3D velocity = user.getDirection().scalarMultiply(userConfig.flySpeed);
            user.setVelocity(velocity);

            double halfSize = boulder.getSize() / 2.0;

            Location newBase = user.getLocation().getBlock().getLocation().add(new Vector3D(0.5, 0.5, 0.5)).add(velocity).subtract(halfSize, boulder.getSize() + 1, halfSize);

            boulder.setCanRender(isValidBase(newBase));
            boulder.setBase(newBase);

            return true;
        }

        @Override
        protected boolean removeOnCollision() {
            return false;
        }
    }

    private class TravelState extends ControlState {
        private Vector3D direction;
        private Location start;
        private List<Entity> hitEntities = new ArrayList<>();

        TravelState() {
            this.direction = user.getDirection();
            this.start = boulder.getBase();

            user.setCooldown(getDescription(), userConfig.cooldown);
        }

        @Override
        public boolean updateState() {
            // Refresh the global start timer so it doesn't time out during travel state.
            startTime = System.currentTimeMillis();

            Location newBase = boulder.getBase().add(direction.scalarMultiply(userConfig.shootSpeed));

            if (newBase.distanceSquared(start) > userConfig.shootRange * userConfig.shootRange) {
                state = null;
                return false;
            }

            if (!Game.getProtectionSystem().canBuild(user, newBase)) {
                return false;
            }

            boulder.setBase(newBase);

            double halfSize = Math.floor(boulder.getSize() / 2.0);
            Location boulderCenter = boulder.getBase().add(halfSize, halfSize, halfSize);

            AABB bounds = boulder.getBoundingBox().at(boulder.getBaseBlockLocation());
            CollisionUtil.handleEntityCollisions(user, bounds, (entity) -> {
                if (hitEntities.contains(entity)) return false;

                if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    return false;
                }

                ((LivingEntity)entity).damage(userConfig.shootDamage);

                Vector3D toEntity = entity.getLocation().subtract(boulderCenter).toVector();
                toEntity = VectorUtil.setY(toEntity, userConfig.shootKnockup);
                Vector3D velocity = VectorUtil.normalizeOrElse(toEntity, Vector3D.PLUS_I);

                entity.setVelocity(velocity.scalarMultiply(userConfig.shootKnockback));
                hitEntities.add(entity);
                return false;
            }, true);

            return true;
        }

        @Override
        protected boolean removeOnCollision() {
            return true;
        }
    }

    // Two dimensional layout of a single layer of the boulder.
    private static class Layer {
        private List<Material> states = new ArrayList<>();
        private int size;

        public Layer(int size) {
            this.size = size;

            for (int i = 0; i < size * size; ++i) {
                this.states.add(Material.AIR);
            }
        }

        public Layer(Layer layer) {
            this.size = layer.size;
            this.states = new ArrayList<>(layer.states);
        }

        public int getSize() {
            return size;
        }

        // Gets the state at position (x, y). (0, 0) is top-left.
        public Material getState(int x, int y) {
            return states.get(y * size + x);
        }

        public void setState(int x, int y, Material state) {
            states.set(y * size + x, state);
        }
    }

    private class Boulder {
        private List<Layer> layers = new ArrayList<>();
        private Location base;
        private boolean canRender;

        public Boulder(Block selectedBlock) {
            this.canRender = true;

            for (int i = 0; i < userConfig.radius; ++i) {
                layers.add(new Layer(userConfig.radius));
            }

            double offset = Math.floor(getSize() / 2.0);
            this.base = selectedBlock.getLocation().subtract(offset, getSize() - 1, offset);

            List<Vector3D> invalidColumns = new ArrayList<>();

            // Generate the layers from the world state.
            for (int i = 0; i < layers.size(); ++i) {
                Layer layer = layers.get(i);

                for (int y = 0; y < layer.getSize(); ++y) {
                    for (int x = 0; x < layer.getSize(); ++x) {
                        Location check = base.add(x, i, y);
                        Material type = check.getBlock().getType();

                        if (!MaterialUtil.isEarthbendable(type) || !Game.getProtectionSystem().canBuild(user, check)) {
                            layer.setState(x, y, Material.AIR);
                            invalidColumns.add(new Vector3D(x, y, i));
                            continue;
                        }

                        Material solidType = MaterialUtil.getSolidEarthType(type);
                        // Generate checkerboard pattern
                        if (i % 2 == 0) {
                            if ((x + y) % 2 == 0) {
                                layer.setState(x, y, Material.AIR);
                            } else {
                                layer.setState(x, y, solidType);
                            }
                        } else {
                            if ((x + y) % 2 == 0) {
                                layer.setState(x, y, solidType);
                            } else {
                                layer.setState(x, y, Material.AIR);
                            }
                        }
                    }
                }
            }

            for (Vector3D v : invalidColumns) {
                for (int i = 0; i < (int)v.getZ(); ++i) {
                    layers.get(i).setState((int)v.getX(), (int)v.getY(), Material.AIR);
                }
            }

            invalidateBlockedAreas();
        }

        // Checks all of the blocks of the boulder and marks them as invalid if they aren't earthbendable
        private void update() {
            if (!canRender) {
                return;
            }

            for (int i = 0; i < this.getSize(); ++i) {
                Layer layer = this.getLayer(i);

                for (int y = 0; y < layer.getSize(); ++y) {
                    for (int x = 0; x < layer.getSize(); ++x) {
                        Material state = layer.getState(x, y);

                        if (state != Material.AIR) {
                            Location check = this.base.add(x, i, y);
                            Block block = check.getBlock();

                            if (block.hasBounds() && !MaterialUtil.isEarthbendable(block) || !Game.getProtectionSystem().canBuild(user, check)) {
                                layer.setState(x, y, Material.AIR);
                            }
                        }
                    }
                }
            }
        }

        // Returns a deep copy of the layers
        private List<Layer> getState() {
            return layers.stream().map(Layer::new).collect(Collectors.toList());
        }

        public void setBase(Location base) {
            this.base = base;
        }

        public Location getBase() {
            return this.base;
        }

        public Location getBaseBlockLocation() {
            return this.base.getBlock().getLocation();
        }

        public Layer getLayer(int index) {
            return layers.get(index);
        }

        public int getSize() {
            return layers.size();
        }

        public boolean canRender() {
            return canRender;
        }

        public void setCanRender(boolean renderable) {
            canRender = renderable;
        }

        private void invalidateBlockedAreas() {
            // Check above each column to ensure it can be raised.
            // Invalidate any columns if it can't be.
            for (int y = 0; y < getSize(); ++y) {
                for (int x = 0; x < getSize(); ++x) {
                    for (int i = 0; i < getSize() + 1; ++i) {
                        Block checkBlock = base.add(x, getSize() + i, y).getBlock();

                        if (!MaterialUtil.isTransparent(checkBlock) || !Game.getProtectionSystem().canBuild(user, checkBlock.getLocation())) {
                            for (int layerIndex = 0; layerIndex < getSize(); ++layerIndex) {
                                layers.get(layerIndex).setState(x, y, Material.AIR);
                            }
                        }
                    }
                }
            }
        }

        // Checks if there's any earthbendable materials in the boulder's column.
        public boolean isValidColumn(int x, int y) {
            for (int i = 0; i < getSize(); ++i) {
                if (MaterialUtil.isEarthbendable(layers.get(i).getState(x, y))) {
                    return true;
                }
            }

            return false;
        }

        // Bounding box in local space.
        public AABB getBoundingBox() {
            Vector3D min = Vector3D.ZERO;
            Vector3D max = new Vector3D(getSize(), getSize(), getSize());

            return new AABB(min, max, base.getWorld());
        }

        public List<Block> getBlocks() {
            List<Block> blocks = new ArrayList<>();

            for (int i = 0; i < getSize(); ++i) {
                for (int y = 0; y < getSize(); ++y) {
                    for (int x = 0; x < getSize(); ++x) {
                        if (layers.get(i).getState(x, y) != Material.AIR) {
                            blocks.add(base.add(x, i, y).getBlock());
                        }
                    }
                }
            }

            return blocks;
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.RADIUS)
        public int radius;
        @Attribute(Attributes.CHARGE_TIME)
        public long chargeTime;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.DURATION)
        public long maxDuration;
        public int minColumns;
        public double raiseEntityPush;

        public boolean grabEnabled;
        @Attribute(Attributes.SELECTION)
        public double grabRange;

        @Attribute(Attributes.RANGE)
        public double shootRange;
        @Attribute(Attributes.SPEED)
        public double shootSpeed;
        @Attribute(Attributes.DAMAGE)
        public double shootDamage;
        @Attribute(Attributes.STRENGTH)
        public double shootKnockback;
        @Attribute(Attributes.STRENGTH)
        public double shootKnockup;

        public boolean flyEnabled;
        @Attribute(Attributes.SPEED)
        public double flySpeed;
        public double flyBoundsSize;
        @Attribute(Attributes.DURATION)
        public long flyDuration;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "earthsmash");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(3000);
            radius = abilityNode.getNode("radius").getInt(3);
            chargeTime = abilityNode.getNode("charge-time").getLong(1500);
            selectRange = abilityNode.getNode("select-range").getDouble(12.0);
            maxDuration = abilityNode.getNode("max-duration").getLong(30000);
            minColumns = abilityNode.getNode("min-columns").getInt(3);
            raiseEntityPush = abilityNode.getNode("raise-entity-push").getDouble(0.85);

            grabEnabled = abilityNode.getNode("grab").getNode("enabled").getBoolean(true);
            grabRange = abilityNode.getNode("grab").getNode("range").getDouble(16.0);

            shootRange = abilityNode.getNode("shoot").getNode("range").getDouble(25.0);
            shootSpeed = abilityNode.getNode("shoot").getNode("speed").getDouble(1.0);
            shootDamage = abilityNode.getNode("shoot").getNode("damage").getDouble(5.0);
            shootKnockback = abilityNode.getNode("shoot").getNode("knockback").getDouble(3.5);
            shootKnockup = abilityNode.getNode("shoot").getNode("knockup").getDouble(0.15);

            flyEnabled = abilityNode.getNode("flight").getNode("enabled").getBoolean(true);
            flySpeed = abilityNode.getNode("flight").getNode("speed").getDouble(0.75);
            flyBoundsSize = abilityNode.getNode("flight").getNode("bounds-size").getDouble(3.0);
            flyDuration = abilityNode.getNode("flight").getNode("duration").getLong(3000);

            if (radius < 3) {
                radius = 3;
            }

            if (radius % 2 == 0) {
                ++radius;
            }
        }
    }
}
