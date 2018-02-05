package com.plushnode.atlacore.platform;


import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.util.ParticleEffectUtil;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.util.Color;

public class SpongeParticleEffectRenderer implements ParticleEffectRenderer {
    @Override
    public void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
        ParticleType type = ParticleEffectUtil.toParticleType(effect);

        if (amount <= 0) amount = 1;

        org.spongepowered.api.effect.particle.ParticleEffect spongeEffect =
                org.spongepowered.api.effect.particle.ParticleEffect.builder()
                .type(type)
                .quantity(amount)
                .offset(new Vector3d(offsetX, offsetY, offsetZ))
                .velocity(new Vector3d(speed, speed, speed))
                .build();

        org.spongepowered.api.world.World world = ((WorldWrapper)center.getWorld()).getSpongeWorld();

        world.spawnParticles(spongeEffect, new Vector3d(center.getX(), center.getY(), center.getZ()));
    }

    @Override
    public void displayColored(ParticleEffect effect, int red, int green, int blue, float speed, int amount, Location center, double range) {
        ParticleType type = ParticleEffectUtil.toParticleType(effect);

        if (amount <= 0) amount = 1;

        org.spongepowered.api.effect.particle.ParticleEffect spongeEffect =
                org.spongepowered.api.effect.particle.ParticleEffect.builder()
                        .type(type)
                        .quantity(amount)
                        .offset(new Vector3d(0.0, 0.0, 0.0))
                        .velocity(new Vector3d(speed, speed, speed))
                        .option(ParticleOptions.COLOR, Color.ofRgb(red, green, blue))
                        .build();

        org.spongepowered.api.world.World world = ((WorldWrapper)center.getWorld()).getSpongeWorld();

        world.spawnParticles(spongeEffect, new Vector3d(center.getX(), center.getY(), center.getZ()));
    }
}
