package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
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
    private Boulder boulder;
    private State state;
    private int tick;
    private long startTime;
    private List<Block> initialBlocks = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.tick = 0;
        this.startTime = System.currentTimeMillis();

        if (method == ActivationMethod.Sneak) {
            List<EarthSmash> earthSmashes = Game.getAbilityInstanceManager().getPlayerInstances(user, EarthSmash.class);
            if (!earthSmashes.isEmpty()) {
                Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), config.grabRange, true);

                if (block != null) {
                    EarthSmash eSmash = earthSmashes.get(0);
                    if (eSmash.isBoulderBlock(block)) {
                        eSmash.enterHoldState();
                    }
                }

                return false;
            }
        } else if (method == ActivationMethod.Punch) {
            List<EarthSmash> earthSmashes = Game.getAbilityInstanceManager().getPlayerInstances(user, EarthSmash.class);
            if (!earthSmashes.isEmpty()) {
                EarthSmash eSmash = earthSmashes.get(0);

                if (eSmash.state instanceof HoldState) {
                    Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), config.grabRange, true);
                    if (eSmash.isBoulderBlock(block)) {
                        eSmash.enterTravelState();
                    }
                }
            }

            return false;
        } else {
            return false;
        }

        this.state = new ChargeState();

        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (time > startTime + config.maxDuration) {
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
        if (block.getLocation().distanceSquared(boulder.getBase()) > boulder.getSize() * boulder.getSize()) {
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

    public void enterHoldState() {
        this.state = new HoldState();
    }

    public void enterTravelState() {
        this.state = new TravelState();
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

            if (time >= chargeStartTime + config.chargeTime) {
                Vector3D direction = user.getDirection();
                Location location = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                location = location.add(side.scalarMultiply(0.5));

                Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);

                if (!user.isSneaking()) {
                    Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), config.selectRange, true);
                    if (block == null) {
                        return false;
                    }

                    if (!MaterialUtil.isEarthbendable(block)) {
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

            return count >= config.minColumns;
        }
    }

    // Used for any state that allows the user to control the boulder.
    private abstract class ControlState implements State {
        public abstract boolean updateState();

        @Override
        public boolean update() {
            Location prevBase = boulder.getBase();
            List<Layer> prevBoulderState = boulder.getState();

            if (!updateState()) {
                return false;
            }

            boulder.update();

            renderBoulder(prevBase, prevBoulderState);
            clearRaiseArea(prevBase, prevBoulderState);

            return true;
        }

        private void renderBoulder(Location prevBase, List<Layer> prevBoulderState) {
            List<Layer> currentBoulderState = boulder.getState();

            resetPreviousBoulder(prevBase, prevBoulderState, currentBoulderState);

            for (int y = 0; y < boulder.getSize(); ++y) {
                for (int x = 0; x < boulder.getSize(); ++x) {
                    if (boulder.isValidColumn(x, y)) {
                        for (int i = 0; i < boulder.getSize(); ++i) {
                            Layer layer = currentBoulderState.get(i);

                            Material type = layer.getState(x, y);
                            Location current = boulder.getBase().add(x, i, y);

                            if (current.getBlock().getType() != type) {
                                new TempBlock(current.getBlock(), type, 10000);
                            }
                        }
                    }
                }
            }
        }

        private void clearRaiseArea(Location prevBase, List<Layer> prevBoulderState) {
            if (!prevBase.equals(boulder.getBase())) {
                if (tick < config.radius && tick > 0) {
                    Layer layer = prevBoulderState.get(config.radius / 2);

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

        // TODO: Perform a diff of the states to do a minimal update.
        private void resetPreviousBoulder(Location prevBase, List<Layer> prevBoulderState, List<Layer> currentBoulderState) {
            for (int i = 0; i < boulder.getSize(); ++i) {
                Layer prevLayer = prevBoulderState.get(i);
                Layer currentLayer = currentBoulderState.get(i);

                for (int y = 0; y < currentLayer.getSize(); ++y) {
                    for (int x = 0; x < currentLayer.getSize(); ++x) {
                        Block block = prevBase.add(x, i, y).getBlock();

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
                boulder.setBase(boulder.getBase().add(0, 1, 0));
                nextRaiseTime = time + 50;
            }

            return true;
        }
    }

    private class IdleState extends ControlState {
        @Override
        public boolean updateState() {
            return true;
        }
    }

    // This state is active when the player is holding sneak and using it as a shield.
    private class HoldState extends ControlState {
        @Override
        public boolean updateState() {
            if (!user.isSneaking()) {
                state = new IdleState();
                return true;
            }

            // TODO: Check for collisions. It should move as close to the collision as possible.

            int halfSize = (int)(boulder.getSize() / 2.0);
            Location targetCenter = user.getEyeLocation().add(user.getDirection().scalarMultiply(boulder.getSize() + 2));
            Location newBase = targetCenter.subtract(halfSize, halfSize, halfSize);

            boulder.setBase(newBase);
            return true;
        }
    }

    private class TravelState extends ControlState {
        private Vector3D direction;
        private Location start;

        TravelState() {
            this.direction = user.getDirection();
            this.start = boulder.getBase();

            user.setCooldown(getDescription(), config.cooldown);
        }

        @Override
        public boolean updateState() {
            // TODO: Check for collisions

            // Refresh the global start timer so it doesn't time out during travel state.
            startTime = System.currentTimeMillis();

            Location newBase = boulder.getBase().add(direction.scalarMultiply(config.shootSpeed));

            if (newBase.distanceSquared(start) > config.shootRange * config.shootRange) {
                state = null;
                return false;
            }

            boulder.setBase(newBase);

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

    private static class Boulder {
        private List<Layer> layers = new ArrayList<>();
        private Location base;

        public Boulder(Block selectedBlock) {
            for (int i = 0; i < config.radius; ++i) {
                layers.add(new Layer(config.radius));
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

                        if (!MaterialUtil.isEarthbendable(type)) {
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
            for (int i = 0; i < this.getSize(); ++i) {
                Layer layer = this.getLayer(i);

                for (int y = 0; y < layer.getSize(); ++y) {
                    for (int x = 0; x < layer.getSize(); ++x) {
                        Material state = layer.getState(x, y);

                        if (state != Material.AIR) {
                            Location check = this.base.add(x, i, y);
                            Block block = check.getBlock();

                            if (block.hasBounds() && !MaterialUtil.isEarthbendable(block)) {
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

        public Layer getLayer(int index) {
            return layers.get(index);
        }

        public int getSize() {
            return layers.size();
        }

        private void invalidateBlockedAreas() {
            // Check above each column to ensure it can be raised.
            // Invalidate any columns if it can't be.
            for (int y = 0; y < getSize(); ++y) {
                for (int x = 0; x < getSize(); ++x) {
                    for (int i = 0; i < getSize() + 1; ++i) {
                        Block checkBlock = base.add(x, getSize() + i, y).getBlock();

                        if (!MaterialUtil.isTransparent(checkBlock)) {
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
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public int radius;
        public long chargeTime;
        public double grabRange;
        public double selectRange;
        public double shootRange;
        public double shootSpeed;
        public long maxDuration;
        public int minColumns;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "earthsmash");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(3000);
            radius = abilityNode.getNode("radius").getInt(3);
            chargeTime = abilityNode.getNode("charge-time").getLong(1500);
            grabRange = abilityNode.getNode("grab-range").getDouble(16.0);
            selectRange = abilityNode.getNode("select-range").getDouble(12.0);
            shootRange = abilityNode.getNode("shoot-range").getDouble(25.0);
            shootSpeed = abilityNode.getNode("shoot-speed").getDouble(1.0);
            maxDuration = abilityNode.getNode("max-duration").getLong(30000);
            minColumns = abilityNode.getNode("min-columns").getInt(3);

            if (radius < 3) {
                radius = 3;
            }

            if (radius % 2 == 0) {
                ++radius;
            }
        }
    }
}
