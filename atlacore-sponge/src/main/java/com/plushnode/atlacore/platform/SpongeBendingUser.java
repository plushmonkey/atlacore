package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.conditionals.*;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.game.slots.AbilitySlotContainer;
import com.plushnode.atlacore.game.slots.StandardAbilitySlotContainer;

import java.util.*;

public class SpongeBendingUser extends LivingEntityWrapper implements User {
    private List<AbilitySlotContainer> slotContainers = new ArrayList<>();
    private List<Element> elements = new ArrayList<>();
    private Map<AbilityDescription, Long> cooldowns = new HashMap<>();
    private CompositeBendingConditional bendingConditional = new CompositeBendingConditional();

    public SpongeBendingUser(org.spongepowered.api.entity.living.Living entity) {
        super(entity);

        slotContainers.add(new StandardAbilitySlotContainer());

        bendingConditional.add(
                new CooldownBendingConditional(),
                new ElementBendingConditional(),
                new EnabledBendingConditional(),
                new PermissionBendingConditional(),
                new GameModeBendingConditional(GameMode.SPECTATOR)
        );
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
        slotContainers.get(slotContainers.size() - 1).setAbility(slot, abilityDesc);
    }

    @Override
    public AbilityDescription getSlotAbility(int slot) {
        return slotContainers.get(slotContainers.size() - 1).getAbility(slot);
    }

    @Override
    public AbilityDescription getSelectedAbility() {
        // Non-player bending users don't have anything selected.
        return null;
    }

    @Override
    public void pushSlotContainer(AbilitySlotContainer slotContainer) {
        slotContainers.add(slotContainer);
    }

    @Override
    public void removeSlotContainer(AbilitySlotContainer slotContainer) {
        slotContainers.remove(slotContainer);
    }

    @Override
    public void popSlotContainer() {
        if (slotContainers.size() > 1) {
            slotContainers.remove(slotContainers.size() - 1);
        }
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

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean canBend(AbilityDescription abilityDescription) {
        return bendingConditional.canBend(this, abilityDescription);
    }

    @Override
    public CompositeBendingConditional getBendingConditional() {
        return bendingConditional;
    }

    @Override
    public void setBendingConditional(CompositeBendingConditional cond) {
        this.bendingConditional = cond;
    }

    @Override
    public Inventory getInventory() {
        return new NullInventory();
    }
}
