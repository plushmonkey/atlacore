package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.element.Element;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class BukkitBendingUser extends LivingEntityWrapper implements User {
    private AbilityDescription[] boundAbilities = new AbilityDescription[9];
    private List<Element> elements = new ArrayList<>();
    private Map<AbilityDescription, Long> cooldowns = new HashMap<>();

    public BukkitBendingUser(LivingEntity entity) {
        super(entity);
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
        validateSlots();
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

        setCooldown(abilityDesc, cooldown);
    }

    @Override
    public void setCooldown(AbilityDescription abilityDesc, long duration) {
        long current = cooldowns.getOrDefault(abilityDesc, 0L);

        // Only set cooldown if the new one is larger.
        if (duration > 0 && duration > current - System.currentTimeMillis()) {
            cooldowns.put(abilityDesc, System.currentTimeMillis() + duration);
            Game.plugin.getEventBus().postCooldownAddEvent(this, abilityDesc);
        }
    }

    @Override
    public boolean isOnCooldown(AbilityDescription abilityDesc) {
        if (!cooldowns.containsKey(abilityDesc)) {
            return false;
        }

        long time = System.currentTimeMillis();
        long end = cooldowns.get(abilityDesc);

        return time < end;
    }

    @Override
    public Map<AbilityDescription, Long> getCooldowns() {
        return cooldowns;
    }
}
