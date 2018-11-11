package com.plushnode.atlacore.game.ability.air;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.ability.common.BurstAbility;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.util.VectorUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AirBurst extends BurstAbility {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private long startTime;
    private boolean released;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.startTime = System.currentTimeMillis();
        this.released = false;

        if (method == ActivationMethod.Fall) {
            if (user.getFallDistance() < userConfig.fallThreshold || user.isSneaking()) {
                return false;
            }

            releaseFall();
        }

        return true;
    }

    @Override
    public UpdateResult update() {
        if (!released) {
            // This renders particles to the left if cone is charged and to the right if sphere is charged.
            // Releasing will prefer sphere over cone if it's fully charged.

            boolean coneCharged = isConeCharged();
            boolean sphereCharged = isSphereCharged();

            if (coneCharged) {
                Vector3D direction = user.getDirection();
                Location location = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                location = location.subtract(side.scalarMultiply(0.5));

                // Display air particles to the left of the player.
                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
            }

            if (sphereCharged) {
                Vector3D direction = user.getDirection();
                Location location = user.getEyeLocation().add(direction);

                Vector3D side = VectorUtil.normalizeOrElse(direction.crossProduct(Vector3D.PLUS_J), Vector3D.PLUS_I);
                location = location.add(side.scalarMultiply(0.5));

                // Display air particles to the right of the player.
                Game.plugin.getParticleRenderer().display(ParticleEffect.SPELL, 0.0f, 0.0f, 0.0f, 0.0f, 1, location, 257);
            }

            if (!user.isSneaking()) {
                if (sphereCharged) {
                    createBurst(user, 0.0, Math.PI, Math.toRadians(10), 0, Math.PI * 2, Math.toRadians(10), AirBlast.class);
                    setRenderInterval(userConfig.sphereRenderInterval);
                    setRenderParticleCount(userConfig.sphereParticlesPerBlast);
                    this.released = true;
                    user.setCooldown(this, userConfig.sphereCooldown);
                }

                if (!this.released && coneCharged) {
                    releaseCone();
                }

                if (!this.released) {
                    return UpdateResult.Remove;
                }
            }

            return UpdateResult.Continue;
        }

        return updateBurst() ? UpdateResult.Continue : UpdateResult.Remove;
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
        return "AirBurst";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    public boolean isSphereCharged() {
        return System.currentTimeMillis() >= startTime + userConfig.sphereChargeTime;
    }

    public boolean isConeCharged() {
        return System.currentTimeMillis() >= startTime + userConfig.coneChargeTime;
    }

    public static void activateCone(User user) {
        for (AirBurst burst : Game.getAbilityInstanceManager().getPlayerInstances(user, AirBurst.class)) {
            if (burst.released) continue;

            if (burst.isConeCharged()) {
                burst.releaseCone();
            }
        }
    }

    private void releaseCone() {
        createCone(user, AirBlast.class);
        setRenderInterval(userConfig.coneRenderInterval);
        setRenderParticleCount(userConfig.coneParticlesPerBlast);
        this.released = true;

        user.setCooldown(this, userConfig.coneCooldown);
    }

    private void releaseFall() {
        createBurst(user, Math.toRadians(75), Math.toRadians(105), Math.toRadians(10), 0, Math.PI * 2, Math.toRadians(10), AirBlast.class);

        setRenderInterval(userConfig.fallRenderInterval);
        setRenderParticleCount(userConfig.fallParticlesPerBlast);
        this.released = true;

        user.setCooldown(this, userConfig.fallCooldown);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;

        public int sphereParticlesPerBlast;
        public int sphereRenderInterval;
        @Attribute(Attributes.CHARGE_TIME)
        public int sphereChargeTime;
        @Attribute(Attributes.COOLDOWN)
        public long sphereCooldown;

        public int coneParticlesPerBlast;
        public int coneRenderInterval;
        @Attribute(Attributes.CHARGE_TIME)
        public int coneChargeTime;
        @Attribute(Attributes.COOLDOWN)
        public long coneCooldown;

        public int fallParticlesPerBlast;
        public int fallRenderInterval;
        @Attribute(Attributes.COOLDOWN)
        public long fallCooldown;
        public int fallThreshold;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "air", "airburst");

            enabled = abilityNode.getNode("enabled").getBoolean(true);

            CommentedConfigurationNode sphereNode = abilityNode.getNode("sphere");
            sphereRenderInterval = sphereNode.getNode("render-interval").getInt(0);
            sphereParticlesPerBlast = sphereNode.getNode("particles-per-blast").getInt(1);
            sphereChargeTime = sphereNode.getNode("charge-time").getInt(1750);
            sphereCooldown = sphereNode.getNode("cooldown").getLong(0);

            CommentedConfigurationNode coneNode = abilityNode.getNode("cone");
            coneRenderInterval = coneNode.getNode("render-interval").getInt(0);
            coneParticlesPerBlast = coneNode.getNode("particles-per-blast").getInt(1);
            coneChargeTime = coneNode.getNode("charge-time").getInt(875);
            coneCooldown = coneNode.getNode("cooldown").getLong(0);

            CommentedConfigurationNode fallNode = abilityNode.getNode("fall");
            fallRenderInterval = fallNode.getNode("render-interval").getInt(0);
            fallParticlesPerBlast = fallNode.getNode("particles-per-blast").getInt(1);
            fallCooldown = fallNode.getNode("cooldown").getLong(6000);
            fallThreshold = fallNode.getNode("fall-threshold").getInt(10);
        }
    }
}
