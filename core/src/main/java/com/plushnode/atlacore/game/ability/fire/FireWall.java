package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.OBB;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.LivingEntity;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.util.VectorUtil;
import com.plushnode.atlacore.util.WorldUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class FireWall implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private OBB collider;
    private Collection<Block> blocks;
    private long startTime;
    private long nextRenderTime;
    private long nextDamageTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.startTime = System.currentTimeMillis();

        if (Math.abs(this.user.getPitch()) > userConfig.maxAngle) {
            return false;
        }

        double hw = userConfig.width / 2.0;
        double hh = userConfig.height / 2.0;
        double ht = userConfig.thickness / 2.0;

        AABB aabb = new AABB(new Vector3D(-hw, -hh, -ht), new Vector3D(hw, hh, ht));

        Vector3D right = VectorUtil.normalizeOrElse(user.getDirection().crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);

        Location location = user.getEyeLocation().add(user.getDirection().scalarMultiply(userConfig.range));

        if (!Game.getProtectionSystem().canBuild(user, location)) {
            return false;
        }

        Rotation rot = new Rotation(Vector3D.PLUS_J, Math.toRadians(user.getYaw()));
        rot = rot.applyTo(new Rotation(right, Math.toRadians(user.getPitch())));

        this.collider = new OBB(aabb, rot, user.getWorld()).at(location.toVector());

        blocks = WorldUtil.getNearbyBlocks(location, VectorUtil.getMaxComponent(collider.getHalfExtents()) + 1.0);

        // Grab all of the blocks that collide with the obb.
        // These will be used to increase render performance.
        blocks = blocks.stream()
                .filter((b) -> collider.intersects(AABB.BLOCK_BOUNDS.at(b.getLocation())))
                .collect(Collectors.toList());

        user.setCooldown(this, userConfig.cooldown);
        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (time > this.nextRenderTime) {
            for (Block block : blocks) {
                Location location = block.getLocation().add(0.5, 0.5, 0.5);
                Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.6f, 0.6f, 0.6f, 0.0f, 3, location, 257);
                Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.6f, 0.6f, 0.6f, 0.0f, 1, location, 257);
            }

            this.nextRenderTime = time + userConfig.renderDelay;
        }

        if (time > this.nextDamageTime) {
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).damage(userConfig.damage, user);
                }

                return false;
            }, true);

            this.nextDamageTime = time + userConfig.damageDelay;
        }

        if (time > startTime + userConfig.duration) {
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
        return "FireWall";
    }

    @Override
    public Collection<Collider> getColliders() {
        return Collections.singletonList(this.collider);
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
        @Attribute(Attributes.HEIGHT)
        public double height;
        @Attribute(Attributes.RADIUS)
        public double width;
        public double thickness;
        @Attribute(Attributes.DAMAGE)
        public double damage;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.DURATION)
        public long duration;
        public int maxAngle;
        public int renderDelay;
        public int damageDelay;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "firewall");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(11000);
            height = abilityNode.getNode("height").getDouble(3.0);
            width = abilityNode.getNode("width").getDouble(6.0);
            thickness = abilityNode.getNode("thickness").getDouble(1.0);
            range = abilityNode.getNode("range").getDouble(3.0);
            damage = abilityNode.getNode("damage").getDouble(3.0);
            duration = abilityNode.getNode("duration").getLong(5000);
            maxAngle = abilityNode.getNode("max-angle").getInt(50);
            renderDelay = abilityNode.getNode("render-delay").getInt(250);
            damageDelay = abilityNode.getNode("damage-delay").getInt(500);
        }
    }
}
