package com.plushnode.atlacore.game.ability.water.surge;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.*;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

// This is a shell ability for activating SurgeWall/SurgeWave.
// It should never actually get added to the instances list.
public class Surge implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (method == ActivationMethod.Punch) {
            // Prioritize sourcing SurgeWall before activating a SurgeWave. Exit if something was sourced.
            if (sourceSurgeWall()) {
                return false;
            }

            // Nothing was sourced, so try to activate SurgeWave.
            if (activate(SurgeWave.class, method)) {
                return false;
            }

            // Try to use bottles to activate SurgeWave.
            SurgeWave wave = new SurgeWave();
            if (wave.activate(user, method)) {
                Game.getAbilityInstanceManager().addAbility(user, wave);
                return false;
            }
        } else if (method == ActivationMethod.Sneak) {
            // Prioritize sourcing SurgeWave. Exit if it was sourced.
            if (sourceSurgeWave()) {
                return false;
            }

            // Nothing was sourced, so try to activate SurgeWall.
            if (activate(SurgeWall.class, method)) {
                return false;
            }

            // Try to use bottles to activate SurgeWall.
            SurgeWall wall = new SurgeWall();
            if (wall.activate(user, method)) {
                Game.getAbilityInstanceManager().addAbility(user, wall);
                return false;
            }
        }

        return false;
    }

    // Returns true if an activation attempt was made. Returns false if there weren't any instances.
    private <T extends Ability> boolean activate(Class<T> clazz, ActivationMethod method) {
        List<T> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, clazz);

        if (!instances.isEmpty()) {
            T ability = instances.get(0);

            ability.activate(user, method);
            return true;
        }

        return false;
    }

    private boolean sourceSurgeWall() {
        if (SurgeWall.config.enabled) {
            List<SurgeWall> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, SurgeWall.class);

            if (instances.isEmpty()) {
                SurgeWall wall = new SurgeWall();

                if (wall.activate(user, ActivationMethod.Punch)) {
                    Game.getAbilityInstanceManager().addAbility(user, wall);
                    return true;
                }
            } else {
                SurgeWall wall = instances.get(0);

                // Reactivate the wall so it can try to re-source.
                if (wall.activate(user, ActivationMethod.Punch)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean sourceSurgeWave() {
        if (SurgeWave.config.enabled) {
            List<SurgeWave> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, SurgeWave.class);

            if (instances.isEmpty()) {
                SurgeWave wave = new SurgeWave();

                if (wave.activate(user, ActivationMethod.Sneak)) {
                    Game.getAbilityInstanceManager().addAbility(user, wave);
                    return true;
                }
            } else {
                SurgeWave wave = instances.get(0);

                // Reactivate the wave so it can try to re-source.
                if (wave.activate(user, ActivationMethod.Sneak)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public UpdateResult update() {
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
        return "Surge";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static boolean isSelected(User user) {
        AbilityDescription desc = user.getSelectedAbility();

        return desc != null && "Surge".equals(desc.getName());
    }

    interface State {
        boolean update();
        void onPunch();
        void onSneak();
        Collider getCollider();
    }

    abstract static class SourceState implements State {
        protected User user;
        protected Block sourceBlock;
        private double maxDistance;
        private long timeout;
        private long startTime;

        SourceState(User user, Block sourceBlock, double maxDistance, long timeout) {
            this.user = user;
            this.sourceBlock = sourceBlock;
            this.maxDistance = maxDistance;
            this.timeout = timeout;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public boolean update() {
            if (user.getLocation().distanceSquared(sourceBlock.getLocation()) > maxDistance * maxDistance) {
                return false;
            }

            if (!Surge.isSelected(user)) {
                return false;
            }

            Location renderLocation = this.sourceBlock.getLocation().add(0.5, 1, 0.5);

            Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 1, renderLocation);

            return System.currentTimeMillis() < startTime + timeout;
        }

        @Override
        public Collider getCollider() {
            return null;
        }
    }

    abstract static class RenderState implements State {
        private User user;
        protected Disc disc;
        private List<TempBlock> tempBlocks = new ArrayList<>();
        protected Material type;
        private double radius;

        public RenderState(User user, double radius) {
            this.user = user;
            this.type = Material.WATER;
            this.radius = radius;
        }

        void clear() {
            tempBlocks.forEach(TempBlock::reset);
            tempBlocks.clear();
        }

        void render(Location location, boolean canIntersectUser) {
            clear();

            updateDisc(location);

            List<Block> blocks = new ArrayList<>();

            for (Block block : WorldUtil.getNearbyBlocks(location, radius + 1)) {
                if (MaterialUtil.isTransparent(block)) {
                    if (disc.intersects(AABB.BLOCK_BOUNDS.at(block.getLocation()))) {
                        // Check line of sight to the target block if it's not the center one.
                        if (!block.equals(location.getBlock())) {
                            Location target = block.getLocation().add(0.5, 0.5, 0.5);
                            Vector3D toTarget = target.subtract(location).toVector();
                            Vector3D direction = toTarget.normalize();
                            double distance = toTarget.getNorm();

                            double distSq = RayCaster.cast(user.getWorld(), new Ray(location, direction), distance, false).distanceSquared(location);

                            if (Math.abs(distSq - (distance * distance)) > 0.01) {
                                continue;
                            }
                        }

                        // Only add blocks if they aren't intersecting the user.
                        if (canIntersectUser || !AABB.BLOCK_BOUNDS.at(block.getLocation()).intersects(user.getBounds().at(user.getLocation()))) {
                            blocks.add(block);
                        }
                    }
                }
            }

            for (Block block : blocks) {
                if (Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                    tempBlocks.add(new TempBlock(block, type));
                }
            }
        }

        private void updateDisc(Location location) {
            final double r = radius;
            final double ht = 0.25;

            AABB aabb = new AABB(new Vector3D(-r, -r, -ht), new Vector3D(r, r, ht));
            Vector3D right = VectorUtil.normalizeOrElse(user.getDirection().crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
            Rotation rot = new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw()));
            rot = rot.applyTo(new Rotation(right, Math.toRadians(user.getPitch())));

            this.disc = new Disc(new OBB(aabb, rot, user.getWorld()).at(location), new Sphere(location, r));
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "surge");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
        }
    }
}
