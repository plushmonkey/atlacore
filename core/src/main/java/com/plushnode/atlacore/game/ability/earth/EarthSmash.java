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
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

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

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        if (method != ActivationMethod.Sneak) {
            return false;
        }

        Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), 10.0, true);
        if (block == null) {
            Game.info("Block is null.");
            return false;
        }

        if (!MaterialUtil.isEarthbendable(block)) {
            Game.info("Block is not earthbendable.");
            return false;
        }

        this.boulder = new Boulder(block);
        this.state = new RaiseState();

        return true;
    }

    @Override
    public UpdateResult update() {
        Location prevBase = boulder.getBase();
        List<Layer> prevBoulderState = boulder.getState();

        this.state.update();

        if (this.state == null) {
            Game.info("State is null.");
            return UpdateResult.Remove;
        }

        this.boulder.update();

        List<Layer> currentBoulderState = boulder.getState();

        resetPreviousBoulder(prevBase, prevBoulderState, currentBoulderState);

        for (int i = 0; i < boulder.getSize(); ++i) {
            Layer layer = currentBoulderState.get(i);

            for (int y = 0; y < layer.getSize(); ++y) {
                for (int x = 0; x < layer.getSize(); ++x) {
                    Material type = layer.getState(x, y);
                    Location current = boulder.getBase().add(x, i, y);

                    if (current.getBlock().getType() != type && type != Material.AIR) {
                        new TempBlock(current.getBlock(), type, 10000);
                    }
                }
            }
        }

        Game.info("State is continuing.");

        return UpdateResult.Continue;
    }

    // TODO: Perform a diff of the states to do a minimal update.
    private void resetPreviousBoulder(Location prevBase, List<Layer> prevBoulderState, List<Layer> currentBoulderState) {
        for (int i = 0; i < boulder.getSize(); ++i) {
            Layer prevLayer = prevBoulderState.get(i);
            Layer currentLayer = currentBoulderState.get(i);

            for (int y = 0; y < currentLayer.getSize(); ++y) {
                for (int x = 0; x < currentLayer.getSize(); ++x) {
                    Material prevState = prevLayer.getState(x, y);
                    Material currentState = currentLayer.getState(x, y);

                    Game.getTempBlockService().reset(prevBase.add(x, i, y));
                }
            }
        }
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

    private interface State {
        void update();
    }

    // This state is active after charging. It raises the boulder up from the ground into its idle position.
    private class RaiseState implements State {
        private Location targetBase;
        private long nextRaiseTime;

        RaiseState() {
            targetBase = boulder.getBase().add(0, boulder.getSize(), 0);
            this.nextRaiseTime = 0;
        }

        @Override
        public void update() {
            if (boulder.getBase().getY() >= targetBase.getY()) {
                Game.info("Boulder base is high.");
                state = null;
                return;
            }

            long time = System.currentTimeMillis();

            if (time >= nextRaiseTime) {
                boulder.setBase(boulder.getBase().add(0, 1, 0));
                nextRaiseTime = time + 500;
            }
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "earthsmash");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
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
            layers.add(new Layer(3));
            layers.add(new Layer(3));
            layers.add(new Layer(3));

            double offset = Math.floor(getSize() / 2.0);
            this.base = selectedBlock.getLocation().subtract(offset, 2, offset);

            // Generate the layers from the world state.
            for (int i = 0; i < layers.size(); ++i) {
                Layer layer = layers.get(i);

                for (int y = 0; y < layer.getSize(); ++y) {
                    for (int x = 0; x < layer.getSize(); ++x) {
                        Location check = base.add(x, i, y);
                        Material type = check.getBlock().getType();

                        if (!MaterialUtil.isEarthbendable(type)) {
                            layer.setState(x, y, Material.AIR);
                            continue;
                        }

                        // Generate checkerboard pattern
                        if (i % 2 == 0) {
                            if ((x + y) % 2 == 0) {
                                layer.setState(x, y, Material.AIR);
                            } else {
                                layer.setState(x, y, type);
                            }
                        } else {
                            if ((x + y) % 2 == 0) {
                                layer.setState(x, y, type);
                            } else {
                                layer.setState(x, y, Material.AIR);
                            }
                        }
                    }
                }
            }
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
    }
}
