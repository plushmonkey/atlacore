package com.plushnode.atlacore;

import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.AbilityRegistry;
import com.plushnode.atlacore.element.Element;
import com.plushnode.atlacore.wrappers.LivingEntityWrapper;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BendingUser extends LivingEntityWrapper implements User {
    public BendingUser(LivingEntity entity) {
        super(entity);
    }

    @Override
    public List<Element> getElements() {
        return Arrays.asList(new Element() {
            @Override
            public String getName() {
                return "Fire";
            }

            @Override
            public String getPermission() {
                return "";
            }

            @Override
            public AbilityRegistry getAbilityRegistry() {
                return new AbilityRegistry();
            }

            @Override
            public List<AbilityDescription> getPassives() {
                return new ArrayList<>();
            }

            @Override
            public void addPassive(AbilityDescription passive) {

            }
        });
    }
}
