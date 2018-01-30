package com.plushnode.atlacore.game.ability.fire.sequences;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.geometry.Sphere;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.ParticleStream;
import com.plushnode.atlacore.platform.Entity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

public class FireSpin implements Ability {
    public static Config config = new Config();

    private User user;
    private List<ParticleStream> streams = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;

        if (user.getLocation().getBlock().isLiquid()) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        user.setCooldown(this);

        Location origin = user.getLocation().add(0, 1, 0);

        for (double angle = 0; angle < 360; angle += 5) {
            Vector3D direction = VectorUtil.rotate(Vector3D.PLUS_I, Vector3D.PLUS_J, Math.toRadians(angle)).normalize();

            streams.add(new FireSpinStream(origin, direction));
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        for (Iterator<ParticleStream> iterator = streams.iterator(); iterator.hasNext();) {
            ParticleStream stream = iterator.next();
            if (!stream.update()) {
                iterator.remove();
            }
        }

        return streams.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
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
        return "FireSpin";
    }

    @Override
    public Collection<Collider> getColliders() {
        return streams.stream()
                .map(ParticleStream::getCollider)
                .collect(Collectors.toList());
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            // Get the streams that matches the collider.
            Optional<ParticleStream> result = streams.stream()
                    .filter((stream) -> stream.getCollider() == collision.getFirstCollider())
                    .findAny();

            // Only remove the matched collider.
            if (result.isPresent()) {
                streams.remove(result.get());
            }
        }
    }

    private class FireSpinStream extends ParticleStream {
        public FireSpinStream(Location origin, Vector3D direction) {
            super(user, origin, direction, config.range, config.speed,
                    config.entityCollisionRadius, config.abilityCollisionRadius, config.damage);
        }

        @Override
        public void render() {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
        }

        @Override
        public boolean onEntityHit(Entity entity) {
            if (super.onEntityHit(entity)) {
                Vector3D direction = getLocation().subtract(getOrigin()).toVector().normalize();

                entity.setVelocity(direction.scalarMultiply(config.knockback));
                return true;
            }

            return false;
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        double damage;
        double range;
        double speed;
        double entityCollisionRadius;
        double abilityCollisionRadius;
        double knockback;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "sequences", "firespin");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            range = abilityNode.getNode("range").getDouble(7.0);
            speed = abilityNode.getNode("speed").getDouble(0.3);
            knockback = abilityNode.getNode("knockback").getDouble(3.0);

            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(0.5);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(0.5);

            abilityNode.getNode("speed").setComment("How many meters the streams advance with each tick.");
        }
    }
}
