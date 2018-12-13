package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.MultiAbility;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.game.slots.AbilitySlotContainer;
import com.plushnode.atlacore.game.slots.MultiAbilitySlotContainer;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class WaterArms implements MultiAbility {
    public static final Config config = new Config();

    private User user;
    private long startTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();

        return true;
    }

    @Override
    public UpdateResult update() {
        if (System.currentTimeMillis() > startTime + 10000) {
            return UpdateResult.Remove;
        }
        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "WaterArms";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    @Override
    public AbilitySlotContainer getSlots() {
        List<AbilityDescription> subAbilities = new ArrayList<>();

        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("Freeze"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("Spear"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("Whip"));

        return new MultiAbilitySlotContainer(subAbilities);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
        }
    }
}
