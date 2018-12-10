package com.plushnode.atlacore.game.ability.water.torrent;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.Grid;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class TorrentWave implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private double radius;
    private Location origin;
    private List<TempBlock> tempBlocks = new ArrayList<>();
    private List<Pair<Double, Double>> obstructedRanges = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        this.radius = 3;
        this.origin = user.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);

        if (user.isOnCooldown(getDescription())) {
            return false;
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        this.radius = Math.min(this.radius + userConfig.speed, userConfig.radius);

        render();

        if (this.radius >= userConfig.radius) {
            clear();
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    private void clear() {
        tempBlocks.forEach(TempBlock::reset);
        tempBlocks.clear();
    }

    private void render() {
        clear();

        Grid grid = new Grid((int)userConfig.radius * 2 + 10) {
            @Override
            public void draw() {
                drawCircle(0, 0, (int)radius, (int)radius, 1);
            }
        };

        grid.draw();

        // TODO: Block collision
        int lookup = (int)Math.floor(grid.getGrid().length / 2.0) - 1;
        for (int x = -lookup; x < lookup; ++x) {
            for (int z = -lookup; z < lookup; ++z) {
                if (grid.getValue(x, z) == 1) {
                    for (int i = 0; i < userConfig.height; ++i) {
                        Location location = origin.add(x, i, z).getBlock().getLocation();
                        Block block = location.getBlock();

                        if (MaterialUtil.isTransparent(block) || block.getType() == Material.WATER) {
                            tempBlocks.add(new TempBlock(block, Material.WATER));
                        } else {

                        }
                    }
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
        return "TorrentWave";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.HEIGHT)
        public int height;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "torrent", "wave");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            radius = abilityNode.getNode("radius").getDouble(12.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            height = abilityNode.getNode("height").getInt(3);
        }
    }
}
