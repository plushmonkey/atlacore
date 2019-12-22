package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Sphere;
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
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IceBlast implements Ability {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K,
            Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_K
    );

    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state;
    private RemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (method == ActivationMethod.Sneak) {
            deflect();

            for (IceBlast instance : Game.getAbilityInstanceManager().getPlayerInstances(user, IceBlast.class)) {
                if (instance.state instanceof SourceState) {
                    ((SourceState) instance.state).source();
                    return false;
                }
            }

            SourceState sourceState = new SourceState();

            if (!sourceState.source()) {
                return false;
            }

            state = sourceState;
            this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                    new IsDeadRemovalPolicy(user),
                    new IsOfflineRemovalPolicy(user),
                    new OutOfWorldRemovalPolicy(user)
            );
            return true;
        } else if (method == ActivationMethod.Punch) {
            for (IceBlast instance : Game.getAbilityInstanceManager().getPlayerInstances(user, IceBlast.class)) {
                instance.state.onPunch();
            }
        }

        return false;
    }

    private void deflect() {
        double rangeSq = config.deflectRange * config.deflectRange;
        for (IceBlast instance : Game.getAbilityInstanceManager().getInstances(IceBlast.class)) {
            if (instance.user.equals(user)) continue;
            if (!instance.user.getWorld().equals(user.getWorld())) continue;
            if (!(instance.state instanceof TravelState)) continue;

            TravelState state = (TravelState)instance.state;
            if (state.location.distanceSquared(user.getEyeLocation()) > rangeSq) continue;

            Sphere selectSphere = new Sphere(state.location, config.deflectRadius);

            // Make sure the user is looking close to the instance.
            if (selectSphere.intersects(user.getViewRay())) {
                // Make sure the player has view of the instance that they are trying to deflect.
                if (WorldUtil.canView(user, state.location, config.deflectRange)) {
                    Game.getAbilityInstanceManager().destroyInstance(instance.user, instance);
                }
            }
        }
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove() || !state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        state.onDestroy();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "IceBlast";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    private interface State {
        // Return false to destroy the ability
        boolean update();

        default void onPunch() { }
        default void onDestroy() { }
    }

    private class SourceState implements State {
        private Block sourceBlock;
        private Location sourceLocation;

        public boolean source() {
            // Try to find a new block to source.
            SourceTypes sourceTypes = SourceTypes.of(SourceType.Ice);
            Optional<Block> source = SourceUtil.getSource(user, userConfig.selectRange, sourceTypes);

            if (source.isPresent()) {
                if (Game.getProtectionSystem().canBuild(user, source.get().getLocation())) {
                    setSourceBlock(source.get());
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean update() {
            if (!user.getWorld().equals(sourceBlock.getLocation().getWorld())) {
                return false;
            }

            if (user.getLocation().distanceSquared(sourceLocation) > userConfig.selectMaxDistance * userConfig.selectMaxDistance) {
                return false;
            }

            if (user.getSelectedAbility() != getDescription()) {
                return false;
            }

            Location renderLocation = this.sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return true;
        }

        private void setSourceBlock(Block sourceBlock) {
            this.sourceBlock = sourceBlock;
            this.sourceLocation = sourceBlock.getLocation().add(0.5, 0.5, 0.5);
        }

        @Override
        public void onPunch() {
             state = new TravelState(sourceLocation);
        }
    }

    private class TravelState implements State {
        private Location origin;
        private Location location;
        private Location target;
        private TempBlock tempBlock;

        public TravelState(Location origin) {
            this.origin = origin;
            this.location = origin;
            this.tempBlock = null;
            this.target = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.range));

            user.setCooldown(IceBlast.this);

            TempBlock tempBlock = Game.getTempBlockService().getTempBlock(origin.getBlock());

            if (tempBlock != null) {
                tempBlock.reset();
            }
        }

        @Override
        public boolean update() {
            if (tempBlock != null) {
                this.tempBlock.reset();
                this.tempBlock = null;
            }

            Vector3D direction = target.subtract(location).toVector();

            if (direction.getNormSq() <= 0.0) {
                return false;
            }

            direction = direction.normalize();

            if (location.getBlock().equals(origin.getBlock())) {
                final Vector3D d = direction;

                Block nextBlock = location.add(d).getBlock();
                if (nextBlock.hasBounds() || nextBlock.isLiquid()) {
                    // Find a new direction because the current one will end up inside of a block.
                    List<Vector3D> potential = DIRECTIONS.stream()
                            .filter((v) -> v.dotProduct(d) >= 0)
                            .filter((v) -> {
                                Block checkBlock = location.add(v).getBlock();
                                return !checkBlock.hasBounds() && !checkBlock.isLiquid();
                            }).collect(Collectors.toCollection(ArrayList::new));

                    if (!potential.isEmpty()) {
                        potential.sort((v, v2) -> {
                            if (v.equals(v2)) return 0;

                            return v.dotProduct(d) > v2.dotProduct(d) ? -1 : 1;
                        });
                        direction = potential.get(0);
                    }
                }
            }

            Location previous = location;

            if (location.distanceSquared(target) <= userConfig.speed * userConfig.speed) {
                location = target;
            } else {
                location = location.add(direction.scalarMultiply(userConfig.speed));
            }

            if (!MaterialUtil.isTransparent(location.getBlock()) && location.getBlock().getType() != Material.WATER) {
                return false;
            }

            if (location.distanceSquared(origin) > userConfig.range * userConfig.range) {
                return false;
            }

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            if (!previous.getBlock().equals(origin.getBlock())) {
                // Give some leniency for block collisions
                AABB blockCollider = AABB.BLOCK_BOUNDS.scale(0.25);
                if (CollisionUtil.handleBlockCollisions(blockCollider, previous, location, false).getFirst()) {
                    return false;
                }
            }

            AABB collider = AABB.BLOCK_BOUNDS.scale(userConfig.entityCollisionRadius * 2).at(location);

            boolean hit = CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    ((LivingEntity) entity).damage(userConfig.damage, user);

                    if (userConfig.slowStrength > 0 && userConfig.slowTicks > 0) {
                        PotionEffect effect = Game.plugin.getPotionFactory().createEffect(PotionEffectType.SLOWNESS, userConfig.slowTicks, userConfig.slowStrength - 1);

                        ((LivingEntity) entity).addPotionEffect(effect);

                        Game.plugin.getParticleRenderer().display(ParticleEffect.ITEM_CRACK, rand(), rand(), rand(), 0.5f, 30, this.location, Material.ICE);
                    }
                }
                return true;
            }, true);

            if (hit) {
                return false;
            }

            tempBlock = new TempBlock(location.getBlock(), Material.ICE);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SNOW_SHOVEL, rand(), rand(), rand(), 0.0f, 25, this.location);
            Game.plugin.getParticleRenderer().display(ParticleEffect.ITEM_CRACK, rand(), rand(), rand(), 0.5f, 10, this.location, Material.ICE);

            return true;
        }

        @Override
        public void onDestroy() {
            if (tempBlock != null) {
                tempBlock.reset();
                tempBlock = null;
            }
        }

        private float rand() {
            return (float)(Math.random() * 2.0f) - 1.0f;
        }
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.SELECTION)
        public double selectMaxDistance;
        @Attribute(Attributes.RADIUS)
        public double deflectRadius;
        @Attribute(Attributes.RANGE)
        public double deflectRange;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.DURATION)
        public int slowTicks;
        @Attribute(Attributes.STRENGTH)
        public int slowStrength;

        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "iceblast");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(1500);
            selectRange = abilityNode.getNode("select-range").getDouble(10.0);
            selectMaxDistance = abilityNode.getNode("select-max-distance").getDouble(20.0);
            deflectRadius = abilityNode.getNode("deflect-radius").getDouble(3.0);
            deflectRange = abilityNode.getNode("deflect-range").getDouble(20.0);
            range = abilityNode.getNode("range").getDouble(20.0);
            speed = abilityNode.getNode("speed").getDouble(2.0);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            slowTicks = abilityNode.getNode("slow-ticks").getInt(70);
            slowStrength = abilityNode.getNode("slow-strength").getInt(3);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
        }
    }
}
