package com.plushnode.atlacore;

import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.ability.AbilityRegistry;
import com.plushnode.atlacore.element.Element;
import com.plushnode.atlacore.wrappers.LivingEntityWrapper;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class BukkitBendingUser extends LivingEntityWrapper implements User {
    private AbilityDescription[] boundAbilities = new AbilityDescription[9];
    private List<Element> elements = new ArrayList<>();
    private Map<AbilityDescription, Long> cooldowns = new HashMap<>();

    public BukkitBendingUser(LivingEntity entity) {
        super(entity);

        elements.add(new Element() {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BukkitBendingUser) {
            return entity.equals(((BukkitBendingUser)obj).entity);
        }
        return entity.equals(obj);
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }

    @Override
    public void addElement(Element element) {
        if (!elements.contains(element)) {
            elements.add(element);
        }
    }

    @Override
    public void removeElement(Element element) {
        elements.remove(element);
    }

    @Override
    public List<Element> getElements() {
        return elements;
    }

    @Override
    public boolean hasElement(Element element) {
        return elements.contains(element);
    }

    public boolean canBind(AbilityDescription abilityDesc) {
        return elements.contains(abilityDesc.getElement());
    }

    @Override
    public void clearSlots() {
        for (int i = 1; i <= 9; ++i) {
            setSlotAbility(i, null);
        }
    }

    @Override
    public void setSlotAbility(int slot, AbilityDescription abilityDesc) {
        boundAbilities[slot - 1] = abilityDesc;
    }

    @Override
    public AbilityDescription getSlotAbility(int slot) {
        return boundAbilities[slot - 1];
    }

    @Override
    public AbilityDescription getSelectedAbility() {
        // Non-player bending users don't have anything selected.
        return null;
    }

    @Override
    public void setCooldown(AbilityDescription abilityDesc) {
        long cooldown = abilityDesc.getCooldown();
        cooldowns.put(abilityDesc, System.currentTimeMillis() + cooldown);
    }

    @Override
    public boolean isOnCooldown(AbilityDescription abilityDesc) {
        if (!cooldowns.containsKey(abilityDesc)) {
            return false;
        }

        long time = System.currentTimeMillis();
        long end = cooldowns.get(abilityDesc);
        boolean onCooldown = time < end;

        if (!onCooldown) {
            cooldowns.remove(abilityDesc);
        }

        return onCooldown;
    }
}
