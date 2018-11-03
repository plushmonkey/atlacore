package com.plushnode.atlacore.preset;

import com.plushnode.atlacore.game.ability.AbilityDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Preset {
    private UUID creator;
    private String name;
    private List<AbilityDescription> abilities = new ArrayList<>();

    public Preset(UUID playerUniqueId, String name) {
        this.creator = playerUniqueId;
        this.name = name;
    }

    public UUID getCreatorId() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public void setAbilities(List<AbilityDescription> abilities) {
        this.abilities = abilities;
    }

    public List<AbilityDescription> getAbilities() {
        return abilities;
    }
}
