package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.block.Material;

public interface ParticleEffectRenderer {
    void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center);
    void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, Material material);
    void displayColored(ParticleEffect effect, int red, int green, int blue, float speed, int amount, Location center);
}
