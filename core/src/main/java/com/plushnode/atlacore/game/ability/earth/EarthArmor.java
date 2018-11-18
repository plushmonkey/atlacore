package com.plushnode.atlacore.game.ability.earth;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.ItemStack;
import com.plushnode.atlacore.platform.PotionEffect;
import com.plushnode.atlacore.platform.PotionEffectType;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.TempArmor;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class EarthArmor implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;
    private long startTime;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
        this.startTime = System.currentTimeMillis();

        // TODO: Have blocks travel and then apply temp armor.
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_HELMET), TempArmor.Slot.Helmet, userConfig.duration);
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_CHESTPLATE), TempArmor.Slot.Chestplate, userConfig.duration);
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_LEGGINGS), TempArmor.Slot.Leggings, userConfig.duration);
        Game.getTempArmorService().add(user, new ItemStack(Material.LEATHER_BOOTS), TempArmor.Slot.Boots, userConfig.duration);

        int ticks = (int)(userConfig.duration / 50);
        if (userConfig.absorptionStrength > 0) {
            PotionEffect effect = Game.plugin.getPotionFactory().createEffect(PotionEffectType.ABSORPTION, ticks, userConfig.absorptionStrength - 1);
            effect.apply(user);
        }
        return true;
    }

    @Override
    public UpdateResult update() {
        if (System.currentTimeMillis() >= this.startTime + userConfig.duration) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        if (userConfig.absorptionStrength > 0) {
            user.removePotionEffect(PotionEffectType.ABSORPTION);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getName() {
        return "EarthArmor";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {

    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        @Attribute(Attributes.DURATION)
        public long duration;
        @Attribute(Attributes.STRENGTH)
        public int absorptionStrength;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "earth", "eartharmor");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
            duration = abilityNode.getNode("duration").getLong(10000);
            absorptionStrength = abilityNode.getNode("absorption-strength").getInt(1);
        }
    }
}
