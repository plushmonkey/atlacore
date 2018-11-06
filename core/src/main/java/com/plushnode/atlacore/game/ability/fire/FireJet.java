package com.plushnode.atlacore.game.ability.fire;

import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.Flight;
import com.plushnode.atlacore.util.MaterialUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class FireJet implements Ability {
    public static Config config = new Config();

    private User user;
    private Flight flight;
    private long startTime;
    private double speed;
    private long duration;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();

        Block block = user.getLocation().getBlock();
        boolean ignitable = MaterialUtil.isIgnitable(block);

        if (!ignitable && !MaterialUtil.isAir(block.getType())) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, getDescription(), user.getLocation())) {
            return false;
        }

        this.speed = config.speed;
        this.duration = config.duration;

        this.flight = Flight.get(user);
        user.setCooldown(this);

        // Don't use getDescription in the protection check because it's not a harmless action.
        if (ignitable && Game.getProtectionSystem().canBuild(user, block.getLocation())) {
            // TODO: TempFire
            new TempBlock(block, Material.FIRE, 3000);
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (System.currentTimeMillis() > startTime + duration) {
            return UpdateResult.Remove;
        }

        if (user.getLocation().getBlock().isLiquid()) {
            return UpdateResult.Remove;
        }

        // scale down to 0.5 speed near the end
        double factor = 1.0 - ((time - startTime) / (2.0 * duration));

        this.user.setVelocity(this.user.getDirection().scalarMultiply(speed * factor));
        this.user.setFallDistance(0.0f);

        Location location = user.getLocation();
        Game.plugin.getParticleRenderer().display(ParticleEffect.FLAME, 0.6f, 0.6f, 0.6f, 0.0f, 20, location, 257);
        Game.plugin.getParticleRenderer().display(ParticleEffect.SMOKE, 0.6f, 0.6f, 0.6f, 0.0f, 20, location, 257);

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        flight.release();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "FireJet";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public void setSpeed(double newSpeed) {
        this.speed = newSpeed;
    }

    public void setDuration(long newDuration) {
        this.duration = newDuration;
    }

    public static class Config extends Configurable {
        public boolean enabled;
        public long cooldown;
        public double speed;
        private long duration;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "fire", "firejet");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(7000);
            speed = abilityNode.getNode("speed").getDouble(0.8);
            duration = abilityNode.getNode("duration").getLong(2000);
        }
    }
}
