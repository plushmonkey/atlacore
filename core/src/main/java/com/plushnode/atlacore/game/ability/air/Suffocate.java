package com.plushnode.atlacore.game.ability.air;

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
import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.policies.removal.*;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

public class Suffocate implements Ability {
    public static Config config = new Config();

    private User user;
    private LivingEntity target;
    private long startTime;
    private long nextDamageTime;
    private long nextSlowTime;
    private long nextBlindTime;
    private boolean started;
    private CompositeRemovalPolicy removalPolicy;
    private SuffocatingConditional conditional;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();
        this.started = false;

        target = RayCaster.entityCast(user, new Ray(user.getEyeLocation(), user.getDirection()), config.selectRange, config.selectScale, LivingEntity.class);

        if (target == null) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, target.getLocation())) {
            return false;
        }

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, config.range, () -> target.getLocation()),
                new SwappedSlotsRemovalPolicy<>(user, Suffocate.class),
                new OutOfWorldRemovalPolicy(user),
                new SneakingRemovalPolicy(user, true)
        );

        return true;
    }

    @Override
    public UpdateResult update() {
        long time = System.currentTimeMillis();

        if (this.removalPolicy.shouldRemove()) {
            return UpdateResult.Remove;
        }

        if (time < this.startTime + config.chargeTime) {
            return UpdateResult.Continue;
        }

        if (!Game.getProtectionSystem().canBuild(user, target.getLocation())) {
            return UpdateResult.Remove;
        }

        if (!this.started) {
            this.started = true;
            this.nextDamageTime = startTime + config.damageDelay;
            this.nextSlowTime = startTime + config.slowDelay;
            this.nextBlindTime = startTime + config.blindDelay;

            if (target instanceof User) {
                // Prevent the user from bending.
                conditional = new SuffocatingConditional();
                ((User) target).getBendingConditional().add(conditional);
            }
        }

        if (config.requireConstantAim) {
            AABB bounds = new AABB(new Vector3D(-0.5, -0.5, -0.5), new Vector3D(0.5, 0.5, 0.5))
                    .scale(config.constantAimRadius)
                    .at(target.getLocation());

            Ray ray = new Ray(user.getEyeLocation(), user.getDirection());

            Optional<Double> result = bounds.intersects(ray);
            if (!result.isPresent()) {
                return UpdateResult.Remove;
            }
        }

        handleEffects();
        render();

        return UpdateResult.Continue;
    }

    private void handleEffects() {
        long time = System.currentTimeMillis();

        if (config.damageAmount > 0 && time > this.nextDamageTime) {
            target.damage(config.damageAmount, user);
            this.nextDamageTime = time + config.damageInterval;
        }

        if (config.slowAmplifier > 0 && time >= this.nextSlowTime) {
            PotionEffect effect = Game.plugin.getPotionFactory().createEffect(
                    PotionEffectType.SLOWNESS, config.slowInterval / 50, config.slowAmplifier - 1, true, false);

            target.addPotionEffect(effect);

            this.nextSlowTime = time + config.slowInterval;
        }

        if (config.blindAmplifier > 0 && time >= this.nextBlindTime) {
            PotionEffect effect = Game.plugin.getPotionFactory().createEffect(
                    PotionEffectType.BLINDNESS, config.blindInterval / 50, config.blindAmplifier - 1, true, false);

            target.addPotionEffect(effect);

            this.nextBlindTime = time + config.blindInterval;
        }
    }

    private void render() {
        long time = System.currentTimeMillis();
        double rt = (time - startTime) / (double)config.renderRadiusScaleTime;
        double lt = (time - startTime) / (double)config.renderLayerScaleTime;
        rt = Math.min(1.0, rt);
        lt = Math.min(1.0, lt);

        double r = Math.max(config.renderMinRadius, config.renderMaxRadius * (1.0 - rt));
        double height = r * 2;
        double maxLayers = config.renderLayers;
        double layers = Math.ceil(lt * maxLayers);
        double spacing = height / (maxLayers + 1);
        Location center = target.getLocation().add(0, 1.8 / 2.0, 0);

        for (int i = 1; i <= layers; ++i) {
            double y = (i * spacing) - r;
            double f = 1.0 - (Math.abs(y) * Math.abs(y)) / (r * r);

            for (double theta = 0.0; theta < Math.PI * 2.0; theta += Math.PI * 2.0 / 5.0) {
                double x = r * f * Math.cos(theta);
                double z = r * f * Math.sin(theta);

                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, center.add(x, y, z), 257);
            }
        }
    }

    @Override
    public void destroy() {
        if (this.started && target instanceof User) {
            // Allow the user to bend again.
            ((User) target).getBendingConditional().remove(conditional);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "Suffocate";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    private static class SuffocatingConditional implements BendingConditional {
        @Override
        public boolean canBend(User user, AbilityDescription desc) {
            return false;
        }
    }

    private static class Config extends Configurable {
        boolean enabled;
        long cooldown;
        long chargeTime;
        double range;
        double selectRange;
        double selectScale;
        boolean requireConstantAim;
        double constantAimRadius;

        double damageAmount;
        int damageDelay;
        int damageInterval;

        int slowAmplifier;
        int slowDelay;
        int slowInterval;

        int blindAmplifier;
        int blindDelay;
        int blindInterval;

        int renderRadiusScaleTime;
        int renderLayerScaleTime;
        double renderMaxRadius;
        double renderMinRadius;
        int renderLayers;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "suffocate");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            chargeTime = abilityNode.getNode("charge-time").getLong(500);
            range = abilityNode.getNode("range").getDouble(20.0);
            selectRange = abilityNode.getNode("select-range").getDouble(6.0);
            selectScale = abilityNode.getNode("select-scale").getDouble(2.0);

            requireConstantAim = abilityNode.getNode("constant-aim-required").getBoolean(true);
            constantAimRadius = abilityNode.getNode("constant-aim-radius").getDouble(5.0);

            CommentedConfigurationNode damageNode = abilityNode.getNode("damage");
            damageAmount = damageNode.getNode("amount").getDouble(2.0);
            damageDelay = damageNode.getNode("delay").getInt(2000);
            damageInterval = damageNode.getNode("interval").getInt(1000);

            CommentedConfigurationNode slowNode = abilityNode.getNode("slow");
            slowAmplifier = slowNode.getNode("amount").getInt(1);
            slowDelay = slowNode.getNode("delay").getInt(500);
            slowInterval = slowNode.getNode("interval").getInt(1250);

            CommentedConfigurationNode blindNode = abilityNode.getNode("blind");
            blindAmplifier = blindNode.getNode("amount").getInt(1);
            blindDelay = blindNode.getNode("delay").getInt(2000);
            blindInterval = blindNode.getNode("interval").getInt(1500);

            CommentedConfigurationNode renderNode = abilityNode.getNode("render");
            renderRadiusScaleTime = renderNode.getNode("radius-scale-time").getInt(5000);
            renderLayerScaleTime = renderNode.getNode("layer-scale-time").getInt(7500);
            renderMaxRadius = renderNode.getNode("max-radius").getDouble(3.0);
            renderMinRadius = renderNode.getNode("min-radius").getDouble(0.5);
            renderLayers = renderNode.getNode("layers").getInt(5);
        }
    }
}
