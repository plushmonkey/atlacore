package com.plushnode.atlacore.game.ability.fire.sequences;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.ParticleStream;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FireKick implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private List<ParticleStream> streams = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (method != ActivationMethod.Sequence) return false;

        this.user = user;

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return false;
        }

        this.userConfig = Game.getAttributeSystem().calculate(this, config);

        user.setCooldown(this, userConfig.cooldown);

        Vector3D up = Vector3D.PLUS_J;
        Vector3D lookingDir = user.getDirection();
        Vector3D right = VectorUtil.normalizeOrElse(lookingDir.crossProduct(up), Vector3D.PLUS_I);
        Vector3D rotateAxis = right.crossProduct(lookingDir);

        Vector3D target = user.getEyeLocation().toVector().add(user.getDirection().scalarMultiply(userConfig.range));
        Vector3D direction = target.subtract(user.getLocation().toVector()).normalize();

        for (double degrees = -30; degrees <= 30; degrees += 5) {
            Vector3D streamDirection = VectorUtil.rotate(direction, rotateAxis, Math.toRadians(degrees));

            streams.add(new FireKickStream(user.getLocation(), streamDirection));
        }

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        streams.removeIf(stream -> !stream.update());
        return streams.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    @Override
    public Collection<Collider> getColliders() {
        return streams.stream()
                .map(ParticleStream::getCollider)
                .collect(Collectors.toList());
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "FireKick";
    }

    private class FireKickStream extends ParticleStream {
        public FireKickStream(Location origin, Vector3D direction) {
            super(user, origin, direction, userConfig.range, userConfig.speed,
                    userConfig.entityCollisionRadius, userConfig.abilityCollisionRadius, userConfig.damage);
        }

        @Override
        public void render() {
            Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.2f, 0.2f, 0.2f, 0.0f, 5, location);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.SPEED)
        public double speed;
        @Attribute(Attributes.ENTITY_COLLISION_RADIUS)
        public double entityCollisionRadius;
        @Attribute(Attributes.ABILITY_COLLISION_RADIUS)
        public double abilityCollisionRadius;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "sequences", "firekick");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(6000);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            range = abilityNode.getNode("range").getDouble(7.0);
            speed = abilityNode.getNode("speed").getDouble(1.0);
            entityCollisionRadius = abilityNode.getNode("entity-collision-radius").getDouble(0.5);
            abilityCollisionRadius = abilityNode.getNode("ability-collision-radius").getDouble(0.5);

            abilityNode.getNode("speed").setComment("How many meters the streams advance with each tick.");
        }
    }
}
