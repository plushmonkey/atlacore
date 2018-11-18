package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
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
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.TempArmor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class EarthArmor implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Location location;
    private Location origin;
    private State state;
    private CompositeRemovalPolicy removalPolicy;
    private Material topMaterial;
    private Material bottomMaterial;
    private List<TempBlock> tempBlocks = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);

        Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.selectRange, true);

        if (block == null || !MaterialUtil.isEarthbendable(block)) return false;
        if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) return false;

        Block below = block.getRelative(BlockFace.DOWN);
        Block above = block.getRelative(BlockFace.UP);

        if (!MaterialUtil.isEarthbendable(below)) return false;
        if (!MaterialUtil.isTransparent(above)) return false;
        if (!MaterialUtil.isTransparent(above.getRelative(BlockFace.UP))) return false;

        topMaterial = MaterialUtil.getSolidEarthType(block.getType());
        bottomMaterial = MaterialUtil.getSolidEarthType(below.getType());

        location = block.getLocation();
        origin = location;
        state = new RaiseState();

        user.setCooldown(this, userConfig.cooldown);

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new CannotBendRemovalPolicy(user, getDescription(), true, true),
                new IsDeadRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user)
        );

        return true;
    }

    @Override
    public UpdateResult update() {
        if (this.removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (!state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    private void apply() {
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_HELMET), TempArmor.Slot.Helmet, userConfig.duration);
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_CHESTPLATE), TempArmor.Slot.Chestplate, userConfig.duration);
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_LEGGINGS), TempArmor.Slot.Leggings, userConfig.duration);
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_BOOTS), TempArmor.Slot.Boots, userConfig.duration);

        int ticks = (int)(userConfig.duration / 50);
        if (userConfig.absorptionStrength > 0) {
            PotionEffect effect = Game.plugin.getPotionFactory().createEffect(PotionEffectType.ABSORPTION, ticks, userConfig.absorptionStrength - 1);
            effect.apply(user);
        }
    }

    private void clearTempBlocks() {
        for (TempBlock tempBlock : tempBlocks) {
            tempBlock.reset();
        }

        tempBlocks.clear();
    }

    @Override
    public void destroy() {
        if (userConfig.absorptionStrength > 0) {
            user.removePotionEffect(PotionEffectType.ABSORPTION);
        }

        clearTempBlocks();
        Game.getTempBlockService().reset(origin.getBlock());
        Game.getTempBlockService().reset(origin.getBlock().getRelative(BlockFace.DOWN));
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "EarthArmor";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    private interface State {
        boolean update();
    }

    private abstract class RenderState implements State {
        void render() {
            clearTempBlocks();

            Block block = location.getBlock();
            Block below = block.getRelative(BlockFace.DOWN);

            if (MaterialUtil.isTransparent(block) && !isOriginBlock(block)) {
                tempBlocks.add(new TempBlock(block, topMaterial));
            }

            if (MaterialUtil.isTransparent(below) && !isOriginBlock(below)) {
                tempBlocks.add(new TempBlock(below, bottomMaterial));
            }
        }

        private boolean isOriginBlock(Block block) {
            return block.equals(origin.getBlock()) || block.equals(origin.getBlock().getRelative(BlockFace.DOWN));
        }
    }

    // This is the initial state for raising the blocks up.
    private class RaiseState extends RenderState {
        @Override
        public boolean update() {
            location = location.add(Vector3D.PLUS_J.scalarMultiply(userConfig.speed));

            if (location.distanceSquared(origin) >= 2.0 * 2.0) {
                new TempBlock(origin.getBlock(), Material.AIR);
                new TempBlock(origin.getBlock().getRelative(BlockFace.DOWN), Material.AIR);
                state = new TravelState();
            }

            render();

            return true;
        }
    }

    private class TravelState extends RenderState {
        @Override
        public boolean update() {
            Location target = user.getEyeLocation();

            double nearby = userConfig.speed + 1.0;

            if (target.distanceSquared(location) <= nearby * nearby) {
                apply();
                state = new IdleState();
            } else {
                // Should always be safe to normalize.
                Vector3D direction = target.subtract(location).toVector().normalize();
                location = location.add(direction.scalarMultiply(userConfig.speed));
                render();
            }

            return true;
        }
    }

    private class IdleState implements State {
        private long startTime;

        IdleState() {
            startTime = System.currentTimeMillis();
            clearTempBlocks();
        }

        @Override
        public boolean update() {
            return System.currentTimeMillis() < this.startTime + userConfig.duration;
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.STRENGTH)
        public int absorptionStrength;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.SPEED)
        public double speed;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "eartharmor");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(20000);
            duration = abilityNode.getNode("duration").getLong(6000);
            absorptionStrength = abilityNode.getNode("absorption-strength").getInt(2);
            selectRange = abilityNode.getNode("select-range").getDouble(10.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
        }
    }
}
