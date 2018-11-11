package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.CollisionUtil;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.*;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.BlockFace;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class Tornado implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private long startTime;
    private List<Collider> colliders = new ArrayList<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.startTime = System.currentTimeMillis();

        return true;
    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    @Override
    public UpdateResult update() {
        if (!user.isSneaking() || user.getEyeLocation().getBlock().isLiquid()) {
            return UpdateResult.Remove;
        }

        long time = System.currentTimeMillis();

        if (userConfig.duration > 0 && time > startTime + userConfig.duration) {
            return UpdateResult.Remove;
        }

        double t = Math.min(1.0, (time - startTime) / (double)userConfig.growthTime);
        double height = 2.0 + t * (userConfig.height - 2.0);
        double radius = 2.0 + t * (userConfig.radius - 2.0);

        if (!Game.getProtectionSystem().canBuild(user, user.getLocation())) {
            return UpdateResult.Remove;
        }

        Location base = RayCaster.cast(user, new Ray(user.getEyeLocation(), user.getDirection()), userConfig.range, true, false);
        if (!Game.getProtectionSystem().canBuild(user, base)) {
            return UpdateResult.Remove;
        }

        if (base.getBlock().getRelative(BlockFace.DOWN).getBounds().max() == null) {
            colliders.clear();
            return UpdateResult.Continue;
        }

        colliders.clear();
        for (int i = 0; i < height - 1; ++i) {
            Location location = base.add(0, i, 0);
            double r = 2.0 + (radius - 2.0) * (i / height);
            AABB aabb = new AABB(new Vector3D(-r, 0, -r), new Vector3D(r, 1, r)).at(location);

            colliders.add(new Disc(new OBB(aabb, user.getWorld()), new Sphere(location.toVector(), r, user.getWorld())));
        }

        for (Collider collider : colliders) {
            CollisionUtil.handleEntityCollisions(user, collider, (entity) -> {
                if (entity.equals(user)) {
                    double dy = user.getLocation().getY() - base.getY();
                    double velY = (1.0 - (dy / (userConfig.height - base.getY()))) * 0.6;

                    if (dy >= height * .95) {
                        velY = 0;
                    } else if (dy >= height * .85) {
                        velY = 6.0 * (.95 - dy / height);
                    } else {
                        velY = .6;
                    }

                    Vector3D velocity = user.getDirection();
                    velocity = VectorUtil.setY(velocity, velY);
                    user.setVelocity(velocity.scalarMultiply(t));
                } else {
                    Vector3D position = collider.getPosition();
                    Vector3D normal = VectorUtil.normalizeOrElse(entity.getLocation().subtract(position).toVector(), Vector3D.PLUS_I);
                    // Get the orthogonal vector so the entity is pushed in the direction of the swirl.
                    Vector3D ortho = VectorUtil.normalizeOrElse(normal.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);

                    // Take a combination of the two so they are pushed out at an angle.
                    normal = VectorUtil.normalizeOrElse(ortho.add(normal), Vector3D.PLUS_I);

                    entity.setVelocity(normal.scalarMultiply(t));
                }

                return false;
            }, true, true);
        }

        render(base, t, height, radius);

        return UpdateResult.Continue;
    }

    private void render(Location base, double t, double height, double radius) {
        long time = System.currentTimeMillis();
        int particleCount = (int)Math.ceil(t * userConfig.particlesPerStream);

        double cycleMS = (1000.0 * userConfig.height / userConfig.renderSpeed);

        for (int j = 0; j < userConfig.streams; ++j) {
            double thetaOffset = j * ((Math.PI * 2) / userConfig.streams);

            for (int i = 0; i < particleCount; ++i) {
                double thisTime = time + (i / (double) particleCount) * cycleMS;
                double f = (thisTime - startTime) / cycleMS % 1.0;

                double y = f * height;
                double theta = y + thetaOffset;

                double x = (2.0 + ((radius - 2.0) * f)) * Math.cos(theta);
                double z = (2.0 + ((radius - 2.0) * f)) * Math.sin(theta);

                Location current = base.add(x, y, z);
                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, current, 257);
            }
        }
    }

    @Override
    public void destroy() {
        user.setCooldown(this, userConfig.cooldown);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Tornado";
    }

    @Override
    public List<Collider> getColliders() {
        return colliders;
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
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.RADIUS)
        public double radius;
        @Attribute(Attributes.HEIGHT)
        public double height;
        public double renderSpeed;
        @Attribute(Attributes.RANGE)
        public double range;
        public long growthTime;
        public int streams;
        public int particlesPerStream;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "tornado");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(4000);
            duration = abilityNode.getNode("duration").getLong(8000);
            radius = abilityNode.getNode("radius").getDouble(10.0);
            height = abilityNode.getNode("height").getDouble(15.0);
            range = abilityNode.getNode("range").getDouble(25.0);
            growthTime = abilityNode.getNode("growth-time").getLong(2000);

            CommentedConfigurationNode renderNode = abilityNode.getNode("render");

            renderSpeed = renderNode.getNode("speed").getDouble(4.0);
            streams = renderNode.getNode("streams").getInt(3);
            particlesPerStream = renderNode.getNode("particles-per-stream").getInt(10);
        }
    }
}
