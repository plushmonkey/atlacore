package com.plushnode.atlacore.game.ability.water;

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
import com.plushnode.atlacore.game.ability.common.source.SourceType;
import com.plushnode.atlacore.game.ability.common.source.SourceTypes;
import com.plushnode.atlacore.game.ability.common.source.SourceUtil;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class SurgeWave implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private Surge.State state;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (method == ActivationMethod.Sneak) {
            if (this.state == null || this.state instanceof WaveSourceState) {
                SourceTypes types = SourceTypes.of(SourceType.Water).and(SourceType.Plant);
                Optional<Block> newSource = SourceUtil.getSource(user, userConfig.selectRange, types);

                if (!newSource.isPresent()) {
                    return false;
                }

                this.state = new WaveSourceState(newSource.get());
            } else {
                this.state.onSneak();
            }

            return true;
        } else {
            if (this.state != null) {
                this.state.onPunch();
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
        return "SurgeWave";
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

    private class WaveSourceState extends Surge.SourceState {
        WaveSourceState(Block sourceBlock) {
            super(SurgeWave.this.user, sourceBlock, userConfig.selectMaxDistance, userConfig.selectTimeout);
        }

        @Override
        public void onPunch() {
            state = new TravelState(sourceBlock.getLocation().add(0.5, 1, 0.5));
        }

        @Override
        public void onSneak() {

        }
    }

    private class TravelState extends Surge.RenderState {
        private Location origin;
        private Location location;
        private Vector3D direction;
        private boolean freeze;
        private boolean hit;

        TravelState(Location origin) {
            super(user, userConfig.radius);

            this.origin = origin;
            this.location = origin;

            Location target = RayCaster.cast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.range, false);

            this.direction = VectorUtil.normalizeOrElse(target.subtract(user.getEyeLocation()).toVector(), Vector3D.PLUS_I);
            this.freeze = false;

            user.setCooldown(SurgeWave.this, userConfig.cooldown);
        }

        @Override
        public boolean update() {
            this.location = this.location.add(direction.scalarMultiply(userConfig.speed));

            render(location, true);
            this.hit = false;

            CollisionUtil.handleEntityCollisions(user, this.disc, entity -> {
                if (entity.equals(user)) {
                    knockback(user);
                    return false;
                }

                if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    hit = true;
                    return true;
                }

                knockback((LivingEntity)entity);

                if (freeze) {
                    freeze((LivingEntity)entity);
                    hit = true;
                }

                return !freeze;
            }, true, true);

            if (hit || this.location.distanceSquared(origin) > userConfig.range * userConfig.range) {
                clear();
                return false;
            }

            return true;
        }

        private void knockback(LivingEntity entity) {
            Vector3D knockback = VectorUtil.setY(direction, direction.getY() * userConfig.verticalPush).scalarMultiply(userConfig.knockbackStrength);

            entity.setVelocity(entity.getVelocity().add(knockback));
            entity.setFallDistance(0.0f);
            entity.setFireTicks(0);
        }

        private void freeze(LivingEntity entity) {
            clear();

            for (Block block : WorldUtil.getNearbyBlocks(entity.getLocation(), userConfig.freezeRadius)) {
                if (MaterialUtil.isTransparent(block) || block.getType() == Material.WATER) {
                    if (!AABB.BLOCK_BOUNDS.at(block.getLocation()).contains(entity.getEyeLocation().toVector())) {
                        new TempBlock(block, Material.ICE, userConfig.freezeDuration);
                    }
                }
            }
        }

        @Override
        public void onPunch() {
            this.freeze = true;
        }

        @Override
        public void onSneak() {

        }

        @Override
        public Collider getCollider() {
            return this.disc;
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
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.STRENGTH)
        public double verticalPush;
        @Attribute(Attributes.STRENGTH)
        public double knockbackStrength;
        @Attribute(Attributes.RADIUS)
        public double freezeRadius;
        @Attribute(Attributes.DURATION)
        public int freezeDuration;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "surge", "wave");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            selectRange = abilityNode.getNode("select-range").getDouble(12.0);
            selectMaxDistance = abilityNode.getNode("select-max-distance").getDouble(25.0);
            selectTimeout = abilityNode.getNode("select-timeout").getLong(30000);
            radius = abilityNode.getNode("radius").getDouble(3.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            range = abilityNode.getNode("range").getDouble(20.0);
            verticalPush = abilityNode.getNode("vertical-push").getDouble(0.2);
            knockbackStrength = abilityNode.getNode("knockback-strength").getDouble(1.0);
            freezeRadius = abilityNode.getNode("freeze-radius").getDouble(3.0);
            freezeDuration = abilityNode.getNode("freeze-duration").getInt(10000);
        }
    }
}
