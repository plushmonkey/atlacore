package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CompositeRemovalPolicy implements RemovalPolicy {
    private List<RemovalPolicy> policies = new ArrayList<>();
    private AbilityDescription abilityDesc;

    public CompositeRemovalPolicy(AbilityDescription abilityDesc, List<RemovalPolicy> policies) {
        this.policies = policies;
        this.abilityDesc = abilityDesc;
    }

    public CompositeRemovalPolicy(AbilityDescription abilityDesc, RemovalPolicy... policies) {
        // Create as an ArrayList instead of fixed-sized so policies can be added/removed.
        this.policies = new ArrayList<>(Arrays.asList(policies));
        this.abilityDesc = abilityDesc;
    }

    @Override
    public boolean shouldRemove() {
        if (policies.isEmpty()) return false;

        for (RemovalPolicy policy : policies) {
            if (policy.shouldRemove()) {
                return true;
            }
        }
        return false;
    }

    public void load(ConfigurationNode config, String prefix) {
        if (this.policies.isEmpty()) return;

        String pathPrefix = prefix + ".RemovalPolicy.";

        String[] tokens = pathPrefix.split("\\.");
        ConfigurationNode node = config.getNode((Object)tokens);

        // Load the configuration section for each policy and pass it to the load method.
        for (Iterator<RemovalPolicy> iterator = policies.iterator(); iterator.hasNext(); ) {
            RemovalPolicy policy = iterator.next();

            ConfigurationNode section = node.getNode(policy.getName().toLowerCase());

            if (section != null) {
                boolean enabled = section.getNode("enabled").getBoolean();

                if (!enabled) {
                    iterator.remove();
                    continue;
                }

                policy.load(section);
            }
        }
    }

    @Override
    public void load(ConfigurationNode config) {
        if (this.policies.isEmpty()) return;


        String element = abilityDesc.getElement().getName();
        String abilityName = abilityDesc.getName();

        load(config, "Abilities." + element + "." + abilityName);
    }

    public void addPolicy(RemovalPolicy policy) {
        this.policies.add(policy);
    }

    public void removePolicyType(Class<? extends RemovalPolicy> type) {
        policies.removeIf((policy) -> type.isAssignableFrom(policy.getClass()));
    }

    @Override
    public String getName() {
        return "Composite";
    }
}
