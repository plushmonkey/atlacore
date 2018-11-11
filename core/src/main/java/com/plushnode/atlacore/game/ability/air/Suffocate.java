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
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.game.conditionals.BendingConditional;
import com.plushnode.atlacore.platform.*;
import com.plushnode.atlacore.policies.removal.*;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

public class Suffocate implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
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
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.startTime = System.currentTimeMillis();
        this.started = false;

        target = RayCaster.entityCast(user, new Ray(user.getEyeLocation(), user.getDirection()), userConfig.selectRange, userConfig.selectScale, LivingEntity.class);

        if (target == null) {
            return false;
        }

        if (!Game.getProtectionSystem().canBuild(user, target.getLocation())) {
            return false;
        }

        this.removalPolicy = new CompositeRemovalPolicy(getDescription(),
                new IsDeadRemovalPolicy(user),
                new OutOfRangeRemovalPolicy(user, userConfig.range, () -> target.getLocation()),
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

        if (time < this.startTime + userConfig.chargeTime) {
            return UpdateResult.Continue;
        }

        if (!Game.getProtectionSystem().canBuild(user, target.getLocation())) {
            return UpdateResult.Remove;
        }

        if (!this.started) {
            this.started = true;
            this.nextDamageTime = startTime + userConfig.damageDelay;
            this.nextSlowTime = startTime + userConfig.slowDelay;
            this.nextBlindTime = startTime + userConfig.blindDelay;

            if (target instanceof User) {
                // Prevent the user from bending.
                conditional = new SuffocatingConditional();
                ((User) target).getBendingConditional().add(conditional);
            }
        }

        if (userConfig.requireConstantAim) {
            AABB bounds = new AABB(new Vector3D(-0.5, -0.5, -0.5), new Vector3D(0.5, 0.5, 0.5))
                    .scale(userConfig.constantAimRadius)
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

        if (userConfig.damageAmount > 0 && time > this.nextDamageTime) {
            target.damage(userConfig.damageAmount, user);
            this.nextDamageTime = time + userConfig.damageInterval;
        }

        if (userConfig.slowAmplifier > 0 && time >= this.nextSlowTime) {
            PotionEffect effect = Game.plugin.getPotionFactory().createEffect(
                    PotionEffectType.SLOWNESS, userConfig.slowInterval / 50, userConfig.slowAmplifier - 1, true, false);

            target.addPotionEffect(effect);

            this.nextSlowTime = time + config.slowInterval;
        }

        if (userConfig.blindAmplifier > 0 && time >= this.nextBlindTime) {
            PotionEffect effect = Game.plugin.getPotionFactory().createEffect(
                    PotionEffectType.BLINDNESS, userConfig.blindInterval / 50, userConfig.blindAmplifier - 1, true, false);

            target.addPotionEffect(effect);

            this.nextBlindTime = time + userConfig.blindInterval;
        }
    }

    private void render() {
        long time = System.currentTimeMillis();
        double rt = (time - startTime) / (double)userConfig.renderRadiusScaleTime;
        double lt = (time - startTime) / (double)userConfig.renderLayerScaleTime;
        rt = Math.min(1.0, rt);
        lt = Math.min(1.0, lt);

        double r = Math.max(userConfig.renderMinRadius, userConfig.renderMaxRadius * (1.0 - rt));
        double height = r * 2;
        double maxLayers = userConfig.renderLayers;
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
        user.setCooldown(this, userConfig.cooldown);

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

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.CHARGE_TIME)
        public long chargeTime;
        @Attribute(Attributes.RANGE)
        public double range;
        @Attribute(Attributes.SELECTION)
        public double selectRange;
        @Attribute(Attributes.SELECTION)
        public double selectScale;
        public boolean requireConstantAim;
        public double constantAimRadius;

        @Attribute(Attributes.DAMAGE)
        public double damageAmount;
        @Attribute(Attributes.DURATION)
        public int damageDelay;
        @Attribute(Attributes.DURATION)
        public int damageInterval;

        @Attribute(Attributes.STRENGTH)
        public int slowAmplifier;
        @Attribute(Attributes.DURATION)
        public int slowDelay;
        @Attribute(Attributes.DURATION)
        public int slowInterval;

        @Attribute(Attributes.STRENGTH)
        public int blindAmplifier;
        @Attribute(Attributes.DURATION)
        public int blindDelay;
        @Attribute(Attributes.DURATION)
        public int blindInterval;

        public int renderRadiusScaleTime;
        public int renderLayerScaleTime;
        public double renderMaxRadius;
        public double renderMinRadius;
        public int renderLayers;

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
