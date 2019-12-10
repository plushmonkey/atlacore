package com.plushnode.atlacore.game.ability.water.surge;

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
import com.plushnode.atlacore.game.ability.common.source.SourceType;
import com.plushnode.atlacore.game.ability.common.source.SourceTypes;
import com.plushnode.atlacore.game.ability.common.source.SourceUtil;
import com.plushnode.atlacore.game.ability.water.util.BottleReturn;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class SurgeWall implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Surge.State state = null;
    private boolean usedBottle;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (user.isOnCooldown(getDescription())) {
            return false;
        }

        if (method == ActivationMethod.Punch) {
            if (this.state == null || this.state instanceof WallSourceState) {
                SourceTypes types = SourceTypes.of(SourceType.Water).and(SourceType.Plant).and(SourceType.Ice);
                Optional<Block> newSource = SourceUtil.getSource(user, userConfig.selectRange, types);

                if (!newSource.isPresent()) {
                    return false;
                }

                this.state = new WallSourceState(newSource.get());
            } else {
                this.state.onPunch();
            }
        } else if (method == ActivationMethod.Sneak) {
            if (this.state != null) {
                this.state.onSneak();
            } else {
                if (SourceUtil.emptyBottle(user)) {
                    this.state = new WallState();
                    this.usedBottle = true;
                    return true;
                }

                return false;
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
        if (usedBottle) {
            Location location = RayCaster.cast(user.getWorld(), user.getViewRay(), userConfig.extension, false);
            location = location.getBlock().getLocation().add(0.5, 0.5, 0.5);

            BottleReturn bottleReturn = new BottleReturn(location);

            if (bottleReturn.activate(user, ActivationMethod.Punch)) {
                Game.getAbilityInstanceManager().addAbility(user, bottleReturn);
            }
        }
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

    private class WallSourceState extends Surge.SourceState {
        WallSourceState(Block sourceBlock) {
            super(SurgeWall.this.user, sourceBlock, userConfig.selectMaxDistance, userConfig.selectTimeout);
        }

        @Override
        public void onPunch() {

        }

        @Override
        public boolean onSneak() {
            state = new TravelState(sourceBlock.getLocation());
            return false;
        }
    }

    private class TravelState implements Surge.State {
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

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
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
        public boolean onSneak() {
            return false;
        }

        @Override
        public Collider getCollider() {
            return null;
        }
    }

    private class WallState extends Surge.RenderState {
        private Location location;
        private long nextToggle;

        WallState() {
            super(user, userConfig.radius);
        }

        @Override
        public boolean update() {
            clear();

            double extension = RayCaster.cast(user.getWorld(), user.getViewRay(), userConfig.extension, false).distance(user.getEyeLocation());

            this.location = user.getEyeLocation().add(user.getDirection().scalarMultiply(extension));

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            if (!Surge.isSelected(user)) {
                return false;
            }

            render(this.location);

            if (!user.isSneaking()) {
                clear();
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
        public boolean onSneak() {
            return false;
        }

        @Override
        public Collider getCollider() {
            return this.disc;
        }

        @Override
        protected void updateDisc(Location location) {
            final double r = radius;
            final double ht = 0.25;

            AABB aabb = new AABB(new Vector3D(-r, -r, -ht), new Vector3D(r, r, ht));
            Vector3D right = VectorUtil.normalizeOrElse(user.getDirection().crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
            Rotation rot = new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw()));
            rot = rot.applyTo(new Rotation(right, Math.toRadians(user.getPitch())));

            this.disc = new Disc(new OBB(aabb, rot, user.getWorld()).addPosition(location), new Sphere(location, r));
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
