package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Disc;
import com.plushnode.atlacore.collision.geometry.OBB;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.policies.removal.CompositeRemovalPolicy;
import com.plushnode.atlacore.policies.removal.IsOfflineRemovalPolicy;
import com.plushnode.atlacore.policies.removal.SwappedSlotsRemovalPolicy;
import com.plushnode.atlacore.util.FireTick;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class FireShield implements Ability {
    public static Config config = new Config();

    private User user;
    private Shield shield;
    private long startTime;
    private long nextRender;
    private CompositeRemovalPolicy removalPolicy;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsOfflineRemovalPolicy(user),
                new SwappedSlotsRemovalPolicy<>(user, FireShield.class)
        );

        if (method == ActivationMethod.Punch) {
            this.shield = new DiscShield();
            user.setCooldown(this);
        } else {
            this.shield = new SphereShield();
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return UpdateResult.Remove;
        }

        if (this.removalPolicy.shouldRemove() || this.shield.update()) {
            return UpdateResult.Remove;
        }

        long time = System.currentTimeMillis();

        if (time >= this.nextRender) {
            this.shield.render();
            this.nextRender = time + this.shield.getRenderDelay();
        }

        CollisionUtil.handleEntityCollisions(user, this.shield.getCollider(), (entity) ->{
            if (!Game.getProtectionSystem().canBuild(user, entity.getLocation())) {
                return false;
            }

            FireTick.set(entity, this.shield.getFireTicks());

            return false;
        }, true);

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
        return "FireShield";
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(shield.getCollider());
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    private interface Shield {
        boolean update();
        void render();
        Collider getCollider();
        long getRenderDelay();
        int getFireTicks();
    }

    // Combines an OBB and Sphere to create a disc with thickness.
    private class DiscShield implements Shield {
        private Disc disc;

        public DiscShield() {
            update();
        }

        @Override
        public boolean update() {
            // project disc in front of player
            Location location = user.getEyeLocation().add(user.getDirection().scalarMultiply(config.discExtension));
            double r = config.discRadius;
            double ht = config.discThickness;

            AABB aabb = new AABB(new Vector3D(-r, -r, -ht), new Vector3D(r, r, ht));

            Vector3D right = VectorUtil.normalizeOrElse(user.getDirection().crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);

            // rotate the bounding box so it's lined up with player's view
            Rotation rot = new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw()));
            rot = rot.applyTo(new Rotation(right, Math.toRadians(user.getPitch())));

            this.disc = new Disc(new OBB(aabb, rot, user.getWorld()).at(location), new Sphere(location.toVector(), config.discRadius, user.getWorld()));

            return System.currentTimeMillis() >= startTime + config.discDuration;
        }

        @Override
        public void render() {
            Location center = user.getEyeLocation().add(user.getDirection().scalarMultiply(config.discExtension));
            for (double angle = 0; angle < 360; angle += 20) {
                Vector3D side = VectorUtil.normalizeOrElse(Vector3D.PLUS_J.crossProduct(user.getDirection()), Vector3D.PLUS_I);
                // Rotate it circularly around the user's direction
                Vector3D direction = VectorUtil.rotate(side, user.getDirection(), Math.toRadians(angle));

                for (int i = 0; i < config.discParticleLayers ; ++i) {
                    Location location = center.add(direction.scalarMultiply(config.discRadius * i / config.discParticleLayers));

                    Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
                }
            }
        }

        @Override
        public Collider getCollider() {
            return disc;
        }

        @Override
        public long getRenderDelay() {
            return config.discParticleRenderDelay;
        }

        @Override
        public int getFireTicks() {
            return config.discFireTicks;
        }
    }

    private class SphereShield implements Shield {
        private Sphere sphere;

        public SphereShield() {
            update();
        }

        @Override
        public Collider getCollider() {
            return sphere;
        }

        @Override
        public boolean update() {
            this.sphere = new Sphere(user.getEyeLocation().toVector(), config.shieldRadius, user.getWorld());

            return !user.isSneaking();
        }

        @Override
        public void render() {
            for (Block block : WorldUtil.getNearbyBlocks(user.getEyeLocation(), config.shieldRadius)) {
                Location location = block.getLocation().add(0.5, 0.5, 0.5);

                Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.6f, 0.6f, 0.6f, 0.0f, 1, location, 257);

                if (Math.random() < 1.0 / 3.0) {
                    Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.6f, 0.6f, 0.6f, 0.0f, 1, location, 257);
                }
            }
        }

        @Override
        public long getRenderDelay() {
            return config.shieldParticleRenderDelay;
        }

        @Override
        public int getFireTicks() {
            return config.shieldFireTicks;
        }
    }


    private static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;

        public double discRadius;
        public long discDuration;
        public double discThickness;
        public double discExtension;
        public int discParticleLayers;
        public long discParticleRenderDelay;
        public int discFireTicks;

        public double shieldRadius;
        public long shieldParticleRenderDelay;
        public int shieldFireTicks;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "fireshield");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);

            discRadius = abilityNode.getNode("disc", "radius").getDouble(3.0);
            discThickness = abilityNode.getNode("disc", "thickness").getDouble(1.0);
            discDuration = abilityNode.getNode("disc", "duration").getLong(1000);
            discExtension = abilityNode.getNode("disc", "extension").getDouble(3.0);
            discParticleLayers = abilityNode.getNode("disc", "particle-layers").getInt(5);
            discParticleRenderDelay = abilityNode.getNode("disc", "particle-render-delay").getInt(200);
            discFireTicks = abilityNode.getNode("disc", "fire-ticks").getInt(40);

            shieldParticleRenderDelay = abilityNode.getNode("shield", "particle-render-delay").getInt(200);
            shieldRadius = abilityNode.getNode("shield", "radius").getDouble(4.0);
            shieldFireTicks = abilityNode.getNode("shield", "fire-ticks").getInt(40);
        }
    }
}
