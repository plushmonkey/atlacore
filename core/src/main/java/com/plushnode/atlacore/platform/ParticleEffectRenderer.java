package com.plushnode.atlacore.platform;

public interface ParticleEffectRenderer {
    void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range);
    void displayColored(ParticleEffect effect, int red, int green, int blue, float speed, int amount, Location center, double range);
}
