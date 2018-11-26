package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.policies.removal.CannotBendRemovalPolicy;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsDeadRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirScooter implements Ability {
    private static Config config = new Config();

    private Set<Material> groundMaterials = Stream.of(
            Material.WATER, Material.LAVA
    ).collect(Collectors.toSet());

    private boolean liquidMovement = true;
    private double liquidClimbSpeed = 0.6;

    private User user;
    private Config userConfig;
    private HeightPredictor heightPredictor;
    private DoubleSmoother heightSmoother;
    private CollisionDetector collisionDetector = new RelaxedCollisionDetector();
    private int stuckCount;
    private double verticalPosition;

    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (user instanceof Player) {
            Player player = (Player)user;

            if (player.isSneaking()) {
                return false;
            }
        }

        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirScooter.class)) {
            return false;
        }

        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.heightPredictor = new HeightPredictor(user, userConfig.targetHeight, userConfig.speed);
        this.heightSmoother = new DoubleSmoother(userConfig.heightTolerance);

        double dist = WorldUtil.distanceAboveGround(user, groundMaterials);
        // Only activate AirScooter if the player is in the air and near the ground.
        if ((dist < 0.5 || dist > 5.0) && !user.getLocation().getBlock().isLiquid()) {
            return false;
        }

        AbilityDescription description = Game.getAbilityRegistry().getAbilityDescription(this);

        this.removalPolicy = new CompositeRemovalPolicy(description,
                new CannotBendRemovalPolicy(user, description, true, false),
                new IsOfflineRemovalPolicy(user),
                new IsDeadRemovalPolicy(user)
        );

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        if (this.removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (!Game.getProtectionSystem().canBuild(user, getDescription(), user.getLocation())) {
            return UpdateResult.Remove;
        }

        if (collisionDetector.isColliding(user)) {
            return UpdateResult.Remove;
        }

        double minSpeed = 0.1;

        if (user.getVelocity().getNormSq() < minSpeed * minSpeed) {
            ++stuckCount;
        } else {
            stuckCount = 0;
        }

        if (stuckCount > 10) {
            return UpdateResult.Remove;
        }

        if (!move()) {
            return UpdateResult.Remove;
        }

        render();
        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        user.setCooldown(getDescription(), userConfig.cooldown);
    }

    public void render() {
        double rotationFrequency = 3;

        verticalPosition += (2 * Math.PI) / (20 / rotationFrequency);
        Location base = user.getLocation().clone();

        int horizontalParticles = 10;
        double radius = 0.6;
        for (int i = 0; i < horizontalParticles; ++i) {
            double angle = ((Math.PI * 2) / horizontalParticles) * i;

            double x = radius * Math.cos(angle) * Math.sin(verticalPosition);
            double y = radius * Math.cos(verticalPosition);
            double z = radius * Math.sin(angle) * Math.sin(verticalPosition);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 2, base.add(x, y, z));
        }
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(new Sphere(user.getLocation().toVector(), userConfig.abilityCollisionRadius));
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirScooter";
    }

    private boolean move() {
        Vector3D direction = VectorUtil.clearAxis(user.getDirection(), 1).normalize();

        // How far the player is above the ground.
        double height = WorldUtil.distanceAboveGround(user, groundMaterials);
        double maxHeight = userConfig.targetHeight + 2.0;
        double smoothedHeight = heightSmoother.add(height);

        if (liquidMovement && user.getLocation().getBlock().isLiquid()) {
            height = userConfig.targetHeight * (1.0 - liquidClimbSpeed);
        } else {
            // Destroy ability if player gets too far from ground.
            if (smoothedHeight > maxHeight) {
                return false;
            }
        }

        double predictedHeight = heightPredictor.getPrediction();

        // Calculate the spring force to push the player back to the target height.
        double displacement = height - predictedHeight;
        double force = -userConfig.springStiffness * displacement;

        double maxForce = 0.5;
        if (Math.abs(force) > maxForce) {
            // Cap the force to maxForce so the player isn't instantly pulled to the ground.
            force = force / Math.abs(force) * maxForce;
        }

        Vector3D velocity = direction.scalarMultiply(userConfig.speed);
        // Set y to force.
        velocity = velocity.add(new Vector3D(0, -velocity.getY() + force, 0.0));

        user.setVelocity(velocity);
        user.setFallDistance(0);

        return true;
    }

    private interface CollisionDetector {
        boolean isColliding(User user);
    }

    private abstract class AbstractCollisionDetector implements CollisionDetector {
        protected boolean isCollision(Location location) {
            Block block = location.getBlock();

            return !MaterialUtil.isTransparent(block) || (!liquidMovement && block.isLiquid()) || MaterialUtil.isSolid(block);
        }
    }

    private class RelaxedCollisionDetector extends AbstractCollisionDetector {
        @Override
        public boolean isColliding(User user) {
            // The location in front of the player, where the player will be in one second.
            Location front = user.getEyeLocation().subtract(0.0, 0.5, 0.0);
            Vector3D direction = VectorUtil.normalizeOrElse(VectorUtil.clearAxis(user.getDirection(), 1), Vector3D.ZERO);

            double playerSpeed = VectorUtil.clearAxis(user.getVelocity(), 1).getNorm();

            front = front.add(direction.scalarMultiply(Math.max(userConfig.speed, playerSpeed)));

            return isCollision(front);
        }
    }

    private static class HeightPredictor {
        private static final Vector3D[] DIRECTIONS = {
                new Vector3D(0, 0, 0),
                new Vector3D(1, 0, 0), new Vector3D(-1, 0, 0),
                new Vector3D(0, 1, 0), new Vector3D(0, -1, 0),
                new Vector3D(0, 0, -1), new Vector3D(0, 0, -1)
        };

        private User user;
        private double targetHeight;
        private double speed;

        public HeightPredictor(User user, double targetHeight, double speed) {
            this.user = user;
            this.targetHeight = targetHeight;
            this.speed = speed;
        }

        public double getPrediction() {
            Location location = user.getLocation().clone();
            Vector3D currentDirection = VectorUtil.clearAxis(user.getDirection(), 1).normalize();

            double playerSpeed = VectorUtil.clearAxis(user.getVelocity(), 1).getNorm();

            double s = Math.max(speed, playerSpeed) * 3;
            location = location.add(currentDirection.scalarMultiply(s));

            AABB playerBounds = user.getBounds().at(location);

            // Project the player forward and check all surrounding blocks for collision.
            for (Vector3D direction : DIRECTIONS) {
                Location checkLocation = location.add(direction);

                Block block = checkLocation.getBlock();

                AABB bounds = block.getBounds().at(block.getLocation());

                if (bounds.intersects(playerBounds)) {
                    // Player will collide with a block soon, so try to raise the player over it.
                    return targetHeight + 1.0;
                }
            }

            return targetHeight;
        }
    }

    private static class DoubleSmoother {
        private double[] values;
        private int size;
        private int index;

        public DoubleSmoother(int size) {
            this.size = size;
            this.index = 0;

            values = new double[size];
        }

        public double add(double value) {
            values[index] = value;
            index = (index + 1) % size;
            return get();
        }

        public double get() {
            return Arrays.stream(this.values).sum() / this.size;
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        public double targetHeight;
        public double springStiffness;
        public int heightTolerance;
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        public double abilityCollisionRadius;

        public Config() {
            super();
        }

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airscooter");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            speed = abilityNode.getNode("speed").getDouble(0.6);
            cooldown = abilityNode.getNode("cooldown").getLong(4000);
            targetHeight = abilityNode.getNode("target-height").getDouble(1.25);
            springStiffness = abilityNode.getNode("spring-stiffness").getDouble(0.3);
            heightTolerance = abilityNode.getNode("height-tolerance").getInt(10);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(2.0);

            abilityNode.getNode("target-height").setComment("How far above ground the scooter should try to stabilize at.");
            abilityNode.getNode("spring-stiffness").setComment("How stiff the spring should be. Higher numbers will cause it to jerk into position.");
            abilityNode.getNode("height-tolerance").setComment("Larger numbers make the height requirement more lenient.");
        }
    }
}
