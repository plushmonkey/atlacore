package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.collision.geometry.Sphere;
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
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.Levelled;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

// TODO: This code is very close to being the same as EarthBlast. They should probably be merged eventually.
public class WaterManipulation implements Ability {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K,
            Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_K
    );
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private State state;
    private boolean launched;
    private Block sourceBlock;
    private TempBlock tempBlock;
    private TempBlock[] trailTempBlocks = new TempBlock[2];
    private Location location;
    private Location target;
    private CompositeRemovalPolicy removalPolicy;
    private boolean usedBottle;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.launched = false;
        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, userConfig.sourceSelectRange + 0.5, () -> sourceBlock.getLocation().add(0.5, 0.5, 0.5)),
                new OutOfWorldRemovalPolicy(user)
        );

        if (method == ActivationMethod.Sneak) {
            if (user.isOnCooldown(getDescription())) {
                return false;
            }

            sourceBlock = getSource();

            if (sourceBlock == null) {
                return false;
            }

            this.state = new SourceState();

            return true;
        }

        boolean redirected = redirectAny(user);

        List<WaterManipulation> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, WaterManipulation.class);

        boolean onCooldown = user.isOnCooldown(getDescription());
        if (!onCooldown) {
            List<WaterManipulation> sourcedInstances = instances.stream()
                    .filter(instance -> !instance.launched)
                    .collect(Collectors.toList());

            if (!sourcedInstances.isEmpty()) {
                sourcedInstances.get(0).launch();
                return false;
            }
        }

        List<WaterManipulation> launchedInstances = instances.stream()
                .filter(instance -> instance.launched)
                .collect(Collectors.toList());

        launchedInstances.forEach(WaterManipulation::redirect);

        if (instances.isEmpty() && !redirected && !onCooldown) {
            if (SourceUtil.emptyBottle(user)) {
                this.sourceBlock = user.getEyeLocation().getBlock();
                this.usedBottle = true;
                launch();
                return true;
            }
        }

        return false;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        if (removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (!this.state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        if (this.tempBlock != null) {
            this.tempBlock.reset();
        }

        for (TempBlock trailTempBlock : trailTempBlocks) {
            if (trailTempBlock != null) {
                trailTempBlock.reset();
            }
        }

        if (usedBottle) {
            BottleReturn bottleReturn = new BottleReturn(location);

            if (bottleReturn.activate(user, ActivationMethod.Punch)) {
                Game.getAbilityInstanceManager().addAbility(user, bottleReturn);
            }
        }
    }

    private Block getSource() {
        Optional<Block> optSource = SourceUtil.getSource(user, userConfig.sourceSelectRange, SourceTypes.of(SourceType.Water).and(SourceType.Ice).and(SourceType.Plant));

        if (!optSource.isPresent()) {
            return null;
        }

        Block source = optSource.get();

        if (!Game.getProtectionSystem().canBuild(user, source.getLocation())) return null;

        // Don't select the block if the center of it is too far away.
        if (source.getLocation().add(0.5, 0.5, 0.5).distanceSquared(user.getEyeLocation()) > userConfig.sourceSelectRange * userConfig.sourceSelectRange) {
            return null;
        }

        // Destroy any existing instances that haven't been launched.
        List<WaterManipulation> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, WaterManipulation.class);
        instances.removeIf(instance -> instance.launched);
        instances.forEach(instance -> Game.getAbilityInstanceManager().destroyInstance(user, instance));

        return source;
    }

    private void launch() {
        this.launched = true;
        user.setCooldown(this, userConfig.cooldown);
        this.location = sourceBlock.getLocation().add(0.5, 0.5, 0.5);

        removalPolicy.removePolicyType(OutOfRangeRemovalPolicy.class);

        state = new TravelState();

        redirect();
    }

    private void redirect() {
        this.target = RayCaster.cast(user, new Ray(user.getEyeLocation(), user.getDirection()),
                config.range, false, true, userConfig.entitySelectRadius,
                Collections.singletonList(location.getBlock()));
    }

    private static boolean redirectAny(User user) {
        final double rangeSq = config.redirectSelectRadius * config.redirectSelectRadius;
        boolean redirected = false;

        for (WaterManipulation instance : Game.getAbilityInstanceManager().getInstances(WaterManipulation.class)) {
            if (!instance.launched) continue;
            if (!instance.location.getWorld().equals(user.getWorld())) continue;
            if (instance.user.equals(user)) continue;
            if (instance.location.distanceSquared(user.getEyeLocation()) > rangeSq) continue;

            Sphere selectSphere = new Sphere(instance.location, config.redirectGrabRadius);
            // Make sure the user is looking close to the instance.
            if (selectSphere.intersects(user.getViewRay())) {
                // Make sure the player has view of the instance that they are trying to redirect.
                if (WorldUtil.canView(user, instance.location, config.redirectSelectRadius)) {
                    Game.getAbilityInstanceManager().changeOwner(instance, user);
                    instance.redirect();
                    redirected = true;
                }
            }
        }

        return redirected;
    }

    private interface State {
        // Return false to destroy instance.
        boolean update();
    }

    private class SourceState implements State {
        @Override
        public boolean update() {
            if (!user.getWorld().equals(sourceBlock.getLocation().getWorld())) {
                return false;
            }

            final double rangeSq = userConfig.sourceSelectRange * userConfig.sourceSelectRange;

            if (user.getLocation().distanceSquared(sourceBlock.getLocation()) > rangeSq) {
                return false;
            }

            if (user.getSelectedAbility() != getDescription()) {
                return false;
            }

            Location renderLocation = sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return true;
        }
    }

    private class TravelState implements State {
        @Override
        public boolean update() {
            Vector3D direction = target.subtract(location).toVector();
            if (direction.getNormSq() <= 0.0) {
                return false;
            }

            direction = direction.normalize();

            if (location.getBlock().equals(sourceBlock)) {
                final Vector3D d = direction;

                Block nextBlock = location.add(d.scalarMultiply(userConfig.speed)).getBlock();
                if (nextBlock.hasBounds() || nextBlock.isLiquid()) {
                    // Find a new direction because the current one will end up inside of a block.
                    List<Vector3D> potential = DIRECTIONS.stream()
                            .filter((v) -> v.dotProduct(d) >= 0)
                            .filter((v) -> {
                                Block checkBlock = location.add(v.scalarMultiply(userConfig.speed)).getBlock();
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

            if (location.distanceSquared(target) <= userConfig.speed * userConfig.speed) {
                location = target;
            } else {
                location = location.add(direction.scalarMultiply(userConfig.speed));
            }

            if (location.distanceSquared(sourceBlock.getLocation()) > userConfig.range * userConfig.range) {
                return false;
            }

            if (!Game.getProtectionSystem().canBuild(user, location)) {
                return false;
            }

            Vector3D knockbackDirection = direction.scalarMultiply(userConfig.knockback);

            AABB collider = AABB.BLOCK_BOUNDS.scale(userConfig.entityCollisionRadius * 2).at(location);
            boolean hit = CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                    ((LivingEntity) entity).damage(userConfig.damage, user);
                }

                if (userConfig.knockback > 0.0) {
                    entity.setVelocity(knockbackDirection);
                }

                return true;
            }, true);

            if (hit) {
                return false;
            }

            Block previousBlock = tempBlock != null ? tempBlock.getPreviousState().getBlock() : null;
            Block block = location.getBlock();

            // Advance the TempBlock when the location moves to a new block.
            if (!block.equals(previousBlock)) {
                // Reset the previous TempBlock if it's not the initial one.
                if (!sourceBlock.equals(previousBlock)) {
                    if (tempBlock != null) {
                        tempBlock.reset();
                    }
                }

                if (block.hasBounds()) {
                    tempBlock = null;
                    return false;
                }

                if (!block.equals(sourceBlock)) {
                    for (TempBlock trailTempBlock : trailTempBlocks) {
                        if (trailTempBlock != null) {
                            trailTempBlock.reset();
                        }
                    }

                    if (previousBlock != null) {
                        if (trailTempBlocks[0] != null) {
                            trailTempBlocks[1] = new TempBlock(trailTempBlocks[0].getBlock(), Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(2)));
                        }

                        trailTempBlocks[0] = new TempBlock(previousBlock, Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(1)));
                    }

                    tempBlock = new TempBlock(block, Material.WATER.createBlockData(data -> ((Levelled)data).setLevel(0)));
                }
            }

            return true;
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return "WaterManipulation";
    }

    @Override
    public Collection<Collider> getColliders() {
        if (location != null) {
            AABB bounds = new AABB(Vector3D.ZERO, new Vector3D(1, 1, 1), location.getWorld())
                    .scale(userConfig.abilityCollisionRadius * 2)
                    .at(location.getBlock().getLocation());

            return Collections.singletonList(bounds);
        }

        return Collections.emptyList();
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entitySelectRadius;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.STRENGTH)
        public double knockback;

        @Attribute(Attributes.SELECTION)
        public double sourceSelectRange;
        public double redirectGrabRadius;
        public double redirectSelectRadius;

        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "watermanipulation");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(1000);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            range = abilityNode.getNode("range").getDouble(25.0);
            knockback = abilityNode.getNode("knockback").getDouble(0.3);
            sourceSelectRange = abilityNode.getNode("source-select-range").getDouble(16.0);
            entitySelectRadius = abilityNode.getNode("entity-select-radius").getDouble(3.0);
            redirectGrabRadius = abilityNode.getNode("redirect-grab-radius").getDouble(3.0);
            redirectSelectRadius = abilityNode.getNode("redirect-select-radius").getDouble(16.0);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.5);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(2.0);
        }
    }
}
