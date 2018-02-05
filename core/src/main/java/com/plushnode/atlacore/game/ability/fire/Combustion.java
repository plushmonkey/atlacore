package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.policies.removal.*;
import com.plushnode.atlacore.util.FireTick;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;

public class Combustion implements Ability {
    public static Config config = new Config();

    private User user;
    private State state;
    private Location location;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.state = new ChargeState();

        if (!Game.getAbilityInstanceManager().getPlayerInstances(user, Combustion.class).isEmpty()) {
            return false;
        }

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new CannotBendRemovalPolicy(user, getDescription()),
                new IsOfflineRemovalPolicy(user),
                new IsDeadRemovalPolicy(user),
                new OutOfWorldRemovalPolicy(user),
                new SwappedSlotsRemovalPolicy<>(user, Combustion.class)
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

    @Override
    public void destroy() {

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Combustion";
    }

    @Override
    public Collection<Collider> getColliders() {
        if (state instanceof TravelState) {
            return Collections.singletonList(new Sphere(location.toVector(), config.abilityCollisionRadius));
        }
        return Collections.emptyList();
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    public static void combust(User user) {
        for (Combustion combustion : Game.getAbilityInstanceManager().getPlayerInstances(user, Combustion.class)) {
            combustion.selfCombust();
        }
    }

    private void selfCombust() {
        if (this.state instanceof TravelState) {
            this.state = new CombustState(location);
        }
    }

    private interface State {
        boolean update();
    }

    // This is the initial state of Combustion.
    // It's used to render the particle ring that happens during charging.
    // This state transitions to TravelState if the player stops sneaking after charging is done.
    // This state transitions to CombustState if the player takes damage while charging.
    private class ChargeState implements State {
        private long startTime;
        private int renderPoint;
        private double previousHealth;

        public ChargeState() {
            this.startTime = System.currentTimeMillis();
            this.previousHealth = user.getHealth();
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();

            boolean charged = time >= this.startTime + config.chargeTime;

            if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
                return false;
            }

            if (user.isSneaking()) {
                render();

                if (config.explodeOnHit && user.getHealth() < previousHealth) {
                    // Combust at player's location because they took damage while charging.
                    state = new CombustState(user.getLocation(), true);
                    return true;
                } else {
                    previousHealth = user.getHealth();
                }

                if (charged) {
                    Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, 1.0f, 1.0f, 1.0f, 0.1f, 1, user.getLocation(), 257);
                }
            } else {
                if (charged) {
                    state = new TravelState();
                    return true;
                }

                return false;
            }

            return true;
        }

        private void render() {
            final double size = 1.75;

            for (int i = 0; i < 2; ++i) {
                renderPoint = (renderPoint + (360 / 60)) % 360;

                double theta = Math.toRadians(renderPoint);
                double x = size * Math.cos(theta);
                double z = size * Math.sin(theta);

                Location location = user.getLocation().add(x, 1, z);

                Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.0f, 3, location, 257);
                Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.0f, 0.0f, 0.0f, 0.0f, 4, location, 257);
            }
        }
    }


    // This state is used after the player releases a charged Combustion.
    // It's used for moving and rendering the projectile.
    // This state transitions to CombustState when it collides with terrain or an entity.
    private class TravelState implements State {
        private Vector3D direction;
        private long startTime;

        public TravelState() {
            removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);

            Location origin = user.getEyeLocation().clone();
            origin = origin.setY(origin.getY() - 0.8);
            location = origin.clone();

            this.startTime = System.currentTimeMillis();

            if (config.explodeOnDeath) {
                removalPolicy.removePolicyType(CannotBendRemovalPolicy.class);
                removalPolicy.removePolicyType(IsDeadRemovalPolicy.class);
            }
        }

        @Override
        public boolean update() {
            if (config.explodeOnDeath && user.isDead()) {
                state = new CombustState(location);
                return true;
            }

            // Manually handle the region protection check because the CannotBendRemovalPolicy no longer checks it
            // when explodeOnDeath is true. This stops players from firing Combustion and then walking into a
            // protected area.
            if (config.explodeOnDeath) {
                if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
                    return false;
                }
            }

            direction = user.getDirection();

            long time = System.currentTimeMillis();

            if ((time - startTime) < config.aliveTime) {
                travel();
            } else {
                user.setCooldown(Combustion.this);
                return false;
            }

            return true;
        }

        private void travel() {
            for (double i = 0; i < 10; ++i) {
                Location previous = location;
                location = location.add(direction.scalarMultiply(config.speed / 10));

                render();

                if (CollisionUtil.handleBlockCollisions(user.getWorld(), new Sphere(location.toVector(), 0.5), previous, location, true)) {
                    state = new CombustState(previous);
                    return;
                }

                boolean hit = CollisionUtil.handleEntityCollisions(user, new Sphere(location.toVector(), config.entityCollisionRadius), (entity) -> {
                    location = entity.getLocation();
                    return true;
                }, true);

                if (hit) {
                    state = new CombustState(location);
                    return;
                }
            }
        }

        private void render() {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.03f, 1, location, 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, 0.0f, 0.0f, 0.0f, 0.06f, 1, location, 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.FIREWORKS_SPARK, 0.0f, 0.0f, 0.0f, 0.06f, 1, location, 257);
        }
    }

    // This state is used for doing the explosion.
    // ChargeState can transition to this state if the player takes damage while charging.
    // TravelState can transition to this state if the projectile collides with terrain or an entity.
    private class CombustState implements State {
        private long startTime;
        private boolean waitForRegen;

        public CombustState(Location location) {
            this(location, false);
        }

        public CombustState(Location location, boolean misfire) {
            removalPolicy.removePolicyType(SwappedSlotsRemovalPolicy.class);
            // This stops players from moving into a protected area to bypass the regen wait time.
            removalPolicy.removePolicyType(CannotBendRemovalPolicy.class);

            this.startTime = System.currentTimeMillis();
            this.waitForRegen = true;

            ExplosionMethod explosionMethod;
            if (config.regenBlocks) {
                explosionMethod = new RegenExplosionMethod(config.damageBlocks, config.regenTime);
            } else {
                explosionMethod = new PermanentExplosionMethod(config.damageBlocks);
                waitForRegen = false;
            }

            double modifier = 0;
            if (misfire) {
                modifier = config.misfireModifier;
            }

            int destroyedCount = explosionMethod.explode(location, config.power + modifier, config.damage + modifier, config.fireTick);
            if (destroyedCount <= 0) {
                waitForRegen = false;
            }

            user.setCooldown(Combustion.this);
        }

        @Override
        public boolean update() {
            return waitForRegen && System.currentTimeMillis() < (this.startTime + config.regenTime);
        }
    }

    private interface ExplosionMethod {
        // Returns how many blocks were destroyed.
        int explode(Location location, double size, double damage, int fireTick);
    }

    private abstract class AbstractExplosionMethod implements ExplosionMethod {
        protected List<Material> blocks = Arrays.asList(
                Material.AIR, Material.BEDROCK, Material.CHEST, Material.TRAPPED_CHEST, Material.OBSIDIAN, Material.PORTAL,
                Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME, Material.FIRE, Material.WALL_SIGN, Material.SIGN_POST,
                Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.BANNER,
                Material.WALL_BANNER, Material.DROPPER, Material.FURNACE, Material.DISPENSER, Material.HOPPER,
                Material.BEACON, Material.BARRIER, Material.MOB_SPAWNER
        );

        private boolean destroy;
        private Random rand = new Random();

        public AbstractExplosionMethod(boolean destroy) {
            this.destroy = destroy;
        }

        public int explode(Location location, double size, double damage, int fireTick) {
            render(location);

            if (!destroy) {
                return 0;
            }

            int destroyCount = destroyBlocks(location, (int)size);

            CollisionUtil.handleEntityCollisions(user, new Sphere(location.toVector(), size), (entity) -> {
                if (entity instanceof LivingEntity) {
                    if (Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                        ((LivingEntity) entity).damage(damage, user);
                        FireTick.set(entity, fireTick);
                    }
                }
                return false;
            }, true);

            return destroyCount;
        }

        private void render(Location location) {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.03f, 1, location, 257);

            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257);

            Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.FIREWORKS_SPARK ,(float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.LARGE_SMOKE, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257);
            Game.plugin.getParticleRenderer().display(ParticleEffect.EXPLOSION_HUGE, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 5, location, 257);
        }

        private int destroyBlocks(Location location, int size) {
            int count = 0;

            for (Block block : WorldUtil.getNearbyBlocks(location, size)) {
                if (!Game.getProtectionSystem().canBuild(user, block.getLocation())) {
                    continue;
                }

                if (destroyBlock(block.getLocation())) {
                    ++count;
                }
            }

            return count;
        }

        protected void placeRandomFire(Location location) {
            int chance = rand.nextInt(3);

            Block block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());

            if (chance != 0) return;

            if (MaterialUtil.isIgnitable(block)) {
                new TempBlock(block, Material.FIRE);
            }
        }

        protected void placeRandomBlock(Location location) {
            int chance = rand.nextInt(3);

            if (chance != 0) return;

            Block block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
            if (MaterialUtil.isSolid(block)) {
                Material type = block.getType();

                location.getBlock().setType(type);
            }
        }

        // Returns how many blocks were destroyed.
        public abstract boolean destroyBlock(Location location);
    }

    private class RegenExplosionMethod extends AbstractExplosionMethod {
        private long regenTime;

        public RegenExplosionMethod(boolean destroy, long regenTime) {
            super(destroy);
            this.regenTime = regenTime;
        }

        @Override
        public boolean destroyBlock(Location l) {
            Block block = l.getBlock();

            TempBlock tempBlock = Game.getTempBlockService().getTempBlock(block);
            if (tempBlock != null) {
                tempBlock.reset();
            }

            if (!MaterialUtil.isTransparent(block) && !blocks.contains(block.getType())) {
                new TempBlock(block, Material.AIR, regenTime);
                placeRandomBlock(l);
                placeRandomFire(l);

                return true;
            }

            return false;
        }
    }

    private class PermanentExplosionMethod extends AbstractExplosionMethod {
        public PermanentExplosionMethod(boolean destroy) {
            super(destroy);
        }

        @Override
        public boolean destroyBlock(Location l) {
            Block block = l.getBlock();

            if (!MaterialUtil.isTransparent(block) && !blocks.contains(block.getType())) {
                Block newBlock = l.getWorld().getBlockAt(l);
                newBlock.setType(Material.AIR);
                placeRandomBlock(l);
                placeRandomFire(l);

                return true;
            }

            return false;
        }
    }

    private static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double speed;
        public double damage;
        public double power;
        public int fireTick;
        public long chargeTime;
        public long aliveTime;
        public boolean explodeOnHit;
        public boolean explodeOnDeath;
        public boolean damageBlocks;
        public boolean regenBlocks;
        public double misfireModifier;
        public long regenTime;

        public double entityCollisionRadius;
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "combustion");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(10000);
            aliveTime = abilityNode.getNode("alive-time").getLong(15000);
            speed = abilityNode.getNode("speed").getDouble(2.0);
            damage = abilityNode.getNode("damage").getDouble(4.0);
            power = abilityNode.getNode("power").getDouble(3.0);
            fireTick = abilityNode.getNode("fire-tick").getInt(60);
            chargeTime = abilityNode.getNode("charge-time").getLong(1500);
            explodeOnHit = abilityNode.getNode("explode-on-hit").getBoolean(true);
            explodeOnDeath = abilityNode.getNode("explode-on-death").getBoolean(true);
            damageBlocks = abilityNode.getNode("damage-blocks").getBoolean(true);
            regenBlocks = abilityNode.getNode("regen-blocks").getBoolean(true);
            misfireModifier = abilityNode.getNode("misfire-modifier").getDouble(-1.0);
            regenTime = abilityNode.getNode("regen-time").getLong(10000);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(1.3);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(2.0);
        }
    }
}
