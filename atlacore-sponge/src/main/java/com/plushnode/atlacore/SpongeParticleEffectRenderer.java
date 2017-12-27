package com.plushnode.atlacore;


import com.flowpowered.math.vector.Vector3d;
import com.plushnode.atlacore.util.ParticleEffectUtil;
import com.plushnode.atlacore.wrappers.WorldWrapper;
import org.spongepowered.api.effect.particle.ParticleType;

public class SpongeParticleEffectRenderer implements ParticleEffectRenderer {
    @Override
    public void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
        ParticleType type = ParticleEffectUtil.toParticleType(effect);

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
}
