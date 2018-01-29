package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.geometry.AABB;
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
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class FireShield implements Ability {
    public static Config config = new Config();

    private User user;
    private Disc disc;
    private long startTime;
    private long nextRender;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (method != ActivationMethod.Punch) return false;

        this.user = user;
        this.disc = new Disc();
        this.startTime = System.currentTimeMillis();
        this.nextRender = this.startTime;

        user.setCooldown(this);

        return true;
    }

    @Override
    public UpdateResult update() {
        this.disc.update();

        long time = System.currentTimeMillis();

        if (time >= this.nextRender) {
            this.disc.render();
            this.nextRender = time + config.discParticleRenderDelay;
        }

        if (time >= this.startTime + config.discDuration) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    private Vector3D calculateRotationAxis() {
        Vector3D up = Vector3D.PLUS_J;
        Vector3D lookingDir = user.getDirection();
        Vector3D right = lookingDir.crossProduct(up).normalize();
        return right.crossProduct(lookingDir);
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
        return Collections.singletonList(disc);
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    // Combines an OBB and Sphere to create a disc with thickness.
    private class Disc implements Collider {
        private Sphere sphere;
        private OBB obb;

        public Disc() {
            update();
        }

        @Override
        public boolean intersects(Collider collider) {
            return sphere.intersects(collider) && obb.intersects(collider);
        }

        @Override
        public Vector3D getPosition() {
            return sphere.center;
        }

        @Override
        public Vector3D getHalfExtents() {
            return obb.getHalfExtents();
        }

        @Override
        public boolean contains(Vector3D point) {
            return sphere.contains(point) && obb.contains(point);
        }

        public void update() {
            Vector3D rotateAxis = calculateRotationAxis();

            // project disc in front of player
            Location location = user.getEyeLocation().add(user.getDirection().scalarMultiply(config.discExtension));
            double r = config.discRadius;
            double ht = config.discThickness;

            AABB aabb = new AABB(new Vector3D(-r, -r, -ht), new Vector3D(r, r, ht));
            AABB globalAABB = aabb.at(location);

            // rotate the bounding box so it's lined up with player's view
            Rotation rot = new Rotation(rotateAxis, Math.toRadians(user.getYaw()));

            this.sphere = new Sphere(location.toVector(), config.discRadius);
            this.obb = new OBB(globalAABB, rot);
        }

        public void render() {
            Location center = user.getEyeLocation().add(user.getDirection().scalarMultiply(config.discExtension));
            for (double angle = 0; angle < 360; angle += 20) {
                Vector3D side = Vector3D.PLUS_J.crossProduct(user.getDirection()).normalize();
                // Rotate it circularly around the user's direction
                Vector3D direction = VectorUtil.rotate(side, user.getDirection(), Math.toRadians(angle));

                for (int i = 0; i < config.discParticleLayers ; ++i) {
                    Location location = center.add(direction.scalarMultiply(config.discRadius * i / config.discParticleLayers));

                    Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
                }
            }
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
        }
    }
}
