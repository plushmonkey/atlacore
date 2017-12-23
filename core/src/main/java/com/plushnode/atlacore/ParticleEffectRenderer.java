package com.plushnode.atlacore;

public interface ParticleEffectRenderer {
    void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range);
}
