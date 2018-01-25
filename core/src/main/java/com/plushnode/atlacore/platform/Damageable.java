package com.plushnode.atlacore.platform;

public interface Damageable {
    void damage(double amount);
    void damage(double amount, Entity source);
    double getHealth();
    double getMaxHealth();
    void resetMaxHealth();
    void setHealth(double health);
    void setMaxHealth(double health);
}
