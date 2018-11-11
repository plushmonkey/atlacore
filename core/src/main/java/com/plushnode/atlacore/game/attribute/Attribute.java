package com.plushnode.atlacore.game.attribute;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Attributes.class)
public @interface Attribute {
    String value();
}
