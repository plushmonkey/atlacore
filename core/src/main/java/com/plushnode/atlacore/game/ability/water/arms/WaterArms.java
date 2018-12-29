package com.plushnode.atlacore.game.ability.water.arms;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.*;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.game.slots.AbilitySlotContainer;
import com.plushnode.atlacore.game.slots.MultiAbilitySlotContainer;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.block.Material;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaterArms implements MultiAbility {
    public static final Config config = new Config();

    private User user;
    private Config userConfig;
    private long startTime;
    private Arm leftArm;
    private Arm rightArm;
    private Arm activeArm;
    private Map<Class, Object> instanceData = new HashMap<>();

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        this.startTime = System.currentTimeMillis();
        this.userConfig = Game.getAttributeSystem().calculate(this, config);

        rightArm = new Arm(user, Vector3D.MINUS_I.scalarMultiply(2), userConfig.length);
        leftArm = new Arm(user, Vector3D.PLUS_I.scalarMultiply(2), userConfig.length);
        activeArm = leftArm;

        return true;
    }

    @Override
    public UpdateResult update() {
        if (System.currentTimeMillis() > startTime + 30000 || (!rightArm.isActive() && !leftArm.isActive())) {
            rightArm.clear();
            leftArm.clear();
            return UpdateResult.Remove;
        }

        AbilityDescription desc = user.getSelectedAbility();
        if (desc != null && "WaterArmsFreeze".equals(desc.getName())) {
            rightArm.setMaterial(Material.ICE);
            leftArm.setMaterial(Material.ICE);
        } else {
            rightArm.setMaterial(Material.WATER);
            leftArm.setMaterial(Material.WATER);
        }

        if (rightArm.isActive() && !rightArm.update()) {
            rightArm.setState(null);
            rightArm.clear();
        }

        if (leftArm.isActive() && !leftArm.update()) {
            leftArm.setState(null);
            leftArm.clear();
        }

        return UpdateResult.Continue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstanceData(Class<T> type) {
        Object data = instanceData.get(type);

        if (data != null && data.getClass().isAssignableFrom(type)) {
            return (T)data;
        }

        return null;
    }

    public <T> T putInstanceData(Class<T> type, T data) {
        instanceData.put(type, data);
        return data;
    }

    public Arm getAndToggleArm() {
        Arm result = activeArm;

        toggleArm();

        if (!activeArm.isActive() || !activeArm.canActivate()) {
            toggleArm();
        }

        if (!activeArm.canActivate()) {
            return null;
        }

        return result;
    }

    private void toggleArm() {
        activeArm = activeArm == rightArm ? leftArm : rightArm;
    }

    @Override
    public void destroy() {
        leftArm.clear();
        rightArm.clear();
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

    public static WaterArms getInstance(User user) {
        List<WaterArms> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, WaterArms.class);

        if (instances.isEmpty()) {
            return null;
        }

        return instances.get(0);
    }

    @Override
    public AbilitySlotContainer getSlots() {
        List<AbilityDescription> subAbilities = new ArrayList<>();

        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("WaterArmsPull"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("WaterArmsPunch"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("WaterArmsGrapple"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("WaterArmsGrab"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("WaterArmsFreeze"));
        subAbilities.add(Game.getAbilityRegistry().getAbilityByName("WaterArmsSpear"));

        return new MultiAbilitySlotContainer(subAbilities);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;
        public int length;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "waterarms");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(0);
            length = abilityNode.getNode("length").getInt(4);
        }
    }
}
