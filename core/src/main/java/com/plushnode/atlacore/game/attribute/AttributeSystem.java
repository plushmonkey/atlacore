package com.plushnode.atlacore.game.attribute;

import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.platform.User;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class AttributeSystem {
    private Map<User, List<UserModifier>> modifierMap = new HashMap<>();

    // Add a modifier that's only active according to some policy.
    public UserModifier addModifier(User user, AttributeModifier modifier, ModifyPolicy policy) {
        List<UserModifier> modifiers = modifierMap.computeIfAbsent(user, key -> new ArrayList<>());
        UserModifier userModifier = new UserModifier(modifier, policy);

        modifiers.add(userModifier);

        return userModifier;
    }

    public void clearModifiers(User user) {
        modifierMap.remove(user);
    }

    public boolean removeModifier(User user, AttributeModifier modifier, ModifyPolicy policy) {
        return removeModifier(user, new UserModifier(modifier, policy));
    }

    public boolean removeModifier(User user, UserModifier modifier) {
        List<UserModifier> modifiers = modifierMap.get(user);
        if (modifiers == null) {
            return false;
        }

        boolean result = modifiers.remove(modifier);

        if (modifiers.isEmpty()) {
            modifierMap.remove(user);
        }

        return result;
    }

    // Recalculates all of the config values for the user's instances.
    public void recalculate(User user) {
        Game.getAbilityInstanceManager().getPlayerInstances(user).forEach(Ability::recalculateConfig);
    }

    public <T extends Configurable> T calculate(Ability ability, T oldConfig) {
        T config;
        try {
            config = (T)oldConfig.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return oldConfig;
        }

        User user = ability.getUser();
        if (user == null) {
            return config;
        }

        List<UserModifier> modifiers = modifierMap.get(user);
        if (modifiers == null) {
            return config;
        }

        List<UserModifier> activeModifiers = modifiers.stream()
                .filter(modifier -> modifier.policy.shouldModify(ability))
                .collect(Collectors.toList());

        for (Field field : config.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Attribute.class) || field.isAnnotationPresent(Attributes.class)) {
                boolean wasAccessible = field.isAccessible();

                field.setAccessible(true);
                modifyField(field, config, activeModifiers);
                field.setAccessible(wasAccessible);
            }
        }

        return config;
    }

    private boolean modifyField(Field field, Configurable config, List<UserModifier> userModifiers) {
        double value;
        try {
            value = ((Number)field.get(config)).doubleValue();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        double addOperation = 0.0;
        double multiplyOperation = 1.0;

        List<Double> multiplicativeOperations = new ArrayList<>();

        for (UserModifier userModifier : userModifiers) {
            AttributeModifier modifier = userModifier.modifier;

            if (hasAttribute(field, modifier.getAttribute())) {
                if (modifier.getType() == ModifierOperation.ADDITIVE) {
                    addOperation += modifier.getAmount();
                } else if (modifier.getType() == ModifierOperation.SUMMED_MULTIPLICATIVE) {
                    multiplyOperation += modifier.getAmount();
                } else if (modifier.getType() == ModifierOperation.MULTIPLICATIVE) {
                    multiplicativeOperations.add(modifier.getAmount());
                }
            }
        }

        value = (value + addOperation) * multiplyOperation;

        for (double amount : multiplicativeOperations) {
            value *= amount;
        }

        try {
            Object result = Converters.get(field.getType()).operate(value);

            field.set(config, result);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean hasAttribute(Field field, String attributeName) {
        for (Attribute attribute : field.getAnnotationsByType(Attribute.class)) {
            if (attribute.value().equals(attributeName)) {
                return true;
            }
        }

        return false;
    }

    private static class UserModifier {
        AttributeModifier modifier;
        ModifyPolicy policy;

        UserModifier(AttributeModifier modifier, ModifyPolicy policy) {
            this.modifier = modifier;
            this.policy = policy;
        }
    }

    private interface Converter<T> {
        T operate(double input);
    }

    // Converts a double into some other numeric type
    private static class Converters {
        static Map<Class, Converter> converters = new HashMap<>();

        // TODO: Rounding?
        static {
            converters.put(Double.class, input -> input);
            converters.put(Integer.class, input -> (int)input);
            converters.put(Long.class, input -> (long)input);
            converters.put(double.class, input -> input);
            converters.put(int.class, input -> (int)input);
            converters.put(long.class, input -> (long)input);
        }

        static Converter get(Class clazz) {
            return converters.get(clazz);
        }
    }
}
