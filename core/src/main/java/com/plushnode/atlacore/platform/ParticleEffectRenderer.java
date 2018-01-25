package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.platform.ParticleEffect;

public interface ParticleEffectRenderer {
    void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range);
}
