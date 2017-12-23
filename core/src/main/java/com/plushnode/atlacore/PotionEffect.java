package com.plushnode.atlacore;

public interface PotionEffect {
    boolean apply(LivingEntity entity);
    int getAmplifier();
    int getDuration();
    PotionEffectType getType();
    boolean hasParticles();
    boolean isAmbient();
}
