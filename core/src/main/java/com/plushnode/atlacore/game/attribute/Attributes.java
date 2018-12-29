package com.plushnode.atlacore.game.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attributes {
    Attribute[] value();

    String ENTITY_COLLISION_RADIUS = "EntityCollisionRadius";
    String ABILITY_COLLISION_RADIUS = "AbilityCollisionRadius";
    String RANGE = "Range";
    String SELECTION = "Selection";
    String COOLDOWN = "Cooldown";
    String SPEED = "Speed";
    String STRENGTH = "Strength";
    String DAMAGE = "Damage";
    String CHARGE_TIME = "ChargeTime";
    String DURATION = "Duration";
    String RADIUS = "Radius";
    String HEIGHT = "Height";
    String AMOUNT = "Amount";

    String TYPES[] = {
            Attributes.ENTITY_COLLISION_RADIUS, Attributes.ABILITY_COLLISION_RADIUS, Attributes.RANGE,
            Attributes.SELECTION, Attributes.COOLDOWN, Attributes.SPEED, Attributes.STRENGTH, Attributes.DAMAGE,
            Attributes.CHARGE_TIME, Attributes.DURATION, Attributes.RADIUS, Attributes.HEIGHT, Attributes.AMOUNT
    };
}
