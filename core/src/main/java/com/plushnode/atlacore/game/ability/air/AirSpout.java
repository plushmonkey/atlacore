package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.collision.RayCaster;
import com.plushnode.atlacore.collision.geometry.AABB;
import com.plushnode.atlacore.collision.geometry.Ray;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.Collections;

public class AirSpout implements Ability {
    public static Config config = new Config();

    private User user;
    private AABB collider;
    private Flight flight;
    private long nextRenderTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        if (Game.getAbilityInstanceManager().destroyInstanceType(user, AirSpout.class)) {
            return false;
        }

        if (user.getEyeLocation().getBlock().isLiquid()) {
            return false;
        }

        this.user = user;
        this.nextRenderTime = System.currentTimeMillis();
        this.flight = Flight.get(user);
        this.flight.setFlying(true);

        return true;
    }

    @Override
    public UpdateResult update() {
        double maxHeight = config.height + config.heightBuffer;

        if (!user.canBend(getDescription())) {
            return UpdateResult.Remove;
        }

        if (user.getEyeLocation().getBlock().isLiquid()) {
            return UpdateResult.Remove;
        }

        Location ground = RayCaster.cast(user, new Ray(user.getLocation(), Vector3D.MINUS_J), maxHeight + 1, true, false);

        // Remove if player gets too far away from ground.
        if (ground.distanceSquared(user.getLocation()) > maxHeight * maxHeight) {
            return UpdateResult.Remove;
        }

        double distance = ground.distance(user.getLocation());

        // Remove flight when user goes above the top. This will drop them back down into the acceptable height.
        if (distance > config.height) {
            flight.setFlying(false);
        } else {
            flight.setFlying(true);
        }

        Location mid = ground.add(Vector3D.PLUS_J.scalarMultiply(distance / 2.0));

        // Create a bounding box for collision that extends through the spout from the ground to the player.
        collider = new AABB(new Vector3D(-0.5, -distance / 2.0, -0.5), new Vector3D(0.5, distance / 2.0, 0.5), user.getWorld()).at(mid);

        render(ground);

        return UpdateResult.Continue;
    }

    private void render(Location ground) {
        long time = System.currentTimeMillis();
        if (time < this.nextRenderTime) return;

        double dy = user.getLocation().getY() - ground.getY();

        for (int i = 0; i < dy; ++i) {
            Location location = ground.add(0, i, 0);
            Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.4f, 0.4f, 0.4f, 0.0f, 3, location, 257);
        }

        nextRenderTime = time + config.renderDelay;
    }

    @Override
    public void destroy() {
        flight.setFlying(false);
        flight.release();
        user.setCooldown(this);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "AirSpout";
    }

    @Override
    public Collection<Collider> getColliders() {
        if (this.collider != null) {
            return Collections.singletonList(this.collider);
        }

        return Collections.emptyList();
    }

    @Override
    public void handleCollision(Collision collision) {
        if (collision.shouldRemoveFirst()) {
            Game.getAbilityInstanceManager().destroyInstance(user, this);
        }
    }

    // This modifies a user's velocity to cap it while on spout.
    public static void handleMovement(User user, Vector3D velocity) {
        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName("AirSpout");

        if (!Game.getAbilityInstanceManager().hasAbility(user, desc)) return;

        // Don't consider y in the calculation.
        velocity = VectorUtil.clearAxis(velocity, 1);

        if (velocity.getNormSq() > config.maxSpeed * config.maxSpeed) {
            velocity = velocity.normalize().scalarMultiply(config.maxSpeed);
            user.setVelocity(velocity);
        }
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double height;
        public double heightBuffer;
        public double maxSpeed;
        public int renderDelay;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airspout");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            height = abilityNode.getNode("height").getDouble(14.0);
            heightBuffer = abilityNode.getNode("height-buffer").getDouble(2.0);
            maxSpeed = abilityNode.getNode("max-speed").getDouble(0.2);
            renderDelay = abilityNode.getNode("render-delay").getInt(100);
        }
    }
}
