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
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

public class EarthBlast implements Ability {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K,
            Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_K
    );
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private boolean launched;
    private Block sourceBlock;
    private Material sourceMaterial;
    private TempBlock tempBlock;
    private Location location;
    private Location target;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.launched = false;
        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, userConfig.sourceSelectRange, () -> sourceBlock.getLocation().add(0.5, 0.5, 0.5)),
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

            renderSelectedBlock();
            return true;
        }

        List<EarthBlast> blasts = Game.getAbilityInstanceManager().getPlayerInstances(user, EarthBlast.class);

        if (blasts.isEmpty()) {
            return false;
        }

        if (!user.isOnCooldown(getDescription())) {
            List<EarthBlast> sourcedBlasts = blasts.stream()
                    .filter((eb) -> !eb.launched)
                    .collect(Collectors.toList());

            if (!sourcedBlasts.isEmpty()) {
                sourcedBlasts.get(0).launch();
                return false;
            }
        }

        List<EarthBlast> launchedBlasts = blasts.stream()
                .filter((eb) -> eb.launched)
                .collect(Collectors.toList());

        launchedBlasts.forEach(EarthBlast::redirect);

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

        if (!launched) {
            return UpdateResult.Continue;
        }

        Vector3D direction = target.subtract(location).toVector();
        if (direction.getNormSq() <= 0.0) {
            return UpdateResult.Remove;
        }

        direction = direction.normalize();

        if (location.getBlock().equals(sourceBlock)) {
            final Vector3D d = direction;

            if (location.add(d.scalarMultiply(userConfig.speed)).getBlock().hasBounds()) {
                // Find a new direction because the current one will end up inside of a block.
                List<Vector3D> potential = DIRECTIONS.stream()
                        .filter((v) -> v.dotProduct(d) >= 0)
                        .filter((v) -> !location.add(v.scalarMultiply(userConfig.speed)).getBlock().hasBounds())
                        .collect(Collectors.toCollection(ArrayList<Vector3D>::new));

                if (!potential.isEmpty()) {
                    potential.sort((v, v2) -> v.dotProduct(d) > v2.dotProduct(d) ? -1 : 1);
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
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return UpdateResult.Remove;
        }

        AABB collider = AABB.BLOCK_BOUNDS.scale(userConfig.entityCollisionRadius).at(location);
        boolean hit = CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
            if (Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                ((LivingEntity) entity).damage(userConfig.damage, user);
            }

            return true;
        }, true);

        if (hit) {
            return UpdateResult.Remove;
        }

        Block previousBlock = this.tempBlock.getPreviousState().getBlock();
        Block block = location.getBlock();

        // Advance the TempBlock when the location moves to a new block.
        if (!previousBlock.equals(block)) {
            // Reset the previous TempBlock if it's not the initial one.
            if (!previousBlock.equals(sourceBlock)) {
                this.tempBlock.reset();
            }

            if (block.hasBounds()) {
                this.tempBlock = null;
                return UpdateResult.Remove;
            }

            if (!block.equals(sourceBlock)) {
                this.tempBlock = new TempBlock(block, sourceMaterial);
            }
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        TempBlock tempBlock = Game.getTempBlockService().getTempBlock(sourceBlock);
        if (tempBlock != null) {
            tempBlock.reset();
        }

        if (this.tempBlock != null) {
            this.tempBlock.reset();
        }
    }

    private Block getSource() {
        Block block = RayCaster.blockCast(user.getWorld(), new Ray(user.getEyeLocation(), user.getDirection()), userConfig.sourceSelectRange, true);

        if (block == null || !MaterialUtil.isEarthbendable(block)) return null;
        if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) return null;

        // Don't select the block if the center of it is too far away.
        if (block.getLocation().add(0.5, 0.5, 0.5).distanceSquared(user.getEyeLocation()) > userConfig.sourceSelectRange * userConfig.sourceSelectRange) {
            return null;
        }

        // Destroy any existing blasts that haven't been launched.
        List<EarthBlast> blasts = Game.getAbilityInstanceManager().getPlayerInstances(user, EarthBlast.class);
        blasts.removeIf((eb) -> eb.launched);
        blasts.forEach((b) -> Game.getAbilityInstanceManager().destroyInstance(user, b));

        return block;
    }

    private void renderSelectedBlock() {
        Material type = sourceBlock.getType();
        sourceMaterial = MaterialUtil.getSolidEarthType(type);

        if (sourceMaterial == type) {
            if (type == Material.STONE) {
                sourceMaterial = Material.COBBLESTONE;
            } else {
                sourceMaterial = Material.STONE;
            }
        }

        this.tempBlock = new TempBlock(sourceBlock, sourceMaterial);
    }

    private void launch() {
        this.launched = true;
        user.setCooldown(this, userConfig.cooldown);
        this.location = sourceBlock.getLocation().add(0.5, 0.5, 0.5);
        this.tempBlock = new TempBlock(sourceBlock, Material.AIR);

        removalPolicy.removePolicyType(OutOfRangeRemovalPolicy.class);

        redirect();
    }

    private void redirect() {
        this.target = RayCaster.cast(user, new Ray(user.getEyeLocation(),
                user.getDirection()), 30.0, true, true, userConfig.entitySelectRadius,
                Collections.singletonList(location.getBlock()));
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "EarthBlast";
    }

    @Override
    public Collection<Collider> getColliders() {
        if (location != null) {
            AABB bounds = new AABB(Vector3D.ZERO, new Vector3D(1, 1, 1), location.getWorld())
                    .scale(userConfig.abilityCollisionRadius)
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
        @Attribute(Attributes.SELECTION)
        public double sourceSelectRange;

        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "earthblast");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            range = abilityNode.getNode("range").getDouble(30.0);
            sourceSelectRange = abilityNode.getNode("source-select-range").getDouble(6.0);
            entitySelectRadius = abilityNode.getNode("entity-select-radius").getDouble(3.0);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(2.5);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(2.0);
        }
    }
}
