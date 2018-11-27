package com.plushnode.atlacore.game.ability.water;

import com.plushnode.atlacore.collision.Collision;
import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.game.attribute.Attribute;
import com.plushnode.atlacore.game.attribute.Attributes;
import com.plushnode.atlacore.platform.User;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.util.List;

// This is a shell ability for activating SurgeWall/SurgeWave.
// It should never actually get added to the instances list.
public class Surge implements Ability {
    public static Config config = new Config();

    private User user;
    private Config userConfig;

    @Override
    public boolean activate(User user, ActivationMethod method) {
        this.user = user;
        recalculateConfig();

        if (method == ActivationMethod.Punch) {
            List<SurgeWall> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, SurgeWall.class);

            if (instances.isEmpty() && SurgeWall.config.enabled) {
                SurgeWall wall = new SurgeWall();

                if (wall.activate(user, method)) {
                    Game.getAbilityInstanceManager().addAbility(user, wall);
                }
            } else {
                SurgeWall wall = instances.get(0);

                // Reactivate the wall so it can try to resource.
                wall.activate(user, method);
            }
        } else if (method == ActivationMethod.Sneak) {
            List<SurgeWall> instances = Game.getAbilityInstanceManager().getPlayerInstances(user, SurgeWall.class);

            if (!instances.isEmpty()) {
                SurgeWall wall = instances.get(0);

                wall.activate(user, ActivationMethod.Sneak);
            }
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        return UpdateResult.Remove;
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
        return "Surge";
    }

    @Override
    public void handleCollision(Collision collision) {

    }

    @Override
    public void recalculateConfig() {
        this.userConfig = Game.getAttributeSystem().calculate(this, config);
    }

    public static class Config extends Configurable {
        public boolean enabled;
        @Attribute(Attributes.COOLDOWN)
        public long cooldown;

        @Override
        public void onConfigReload() {
            CommentedConfigurationNode abilityNode = config.getNode("abilities", "water", "surge");

            enabled = abilityNode.getNode("enabled").getBoolean(true);
            cooldown = abilityNode.getNode("cooldown").getLong(500);
        }
    }
}
