package com.plushnode.atlacore.policies.removal;

import com.plushnode.atlacore.ability.AbilityDescription;
import com.plushnode.atlacore.config.Configuration;

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

    public void load(Configuration config, String prefix) {
        if (this.policies.isEmpty()) return;

        String pathPrefix = prefix + ".RemovalPolicy.";

        // Load the configuration section for each policy and pass it to the load method.
        for (Iterator<RemovalPolicy> iterator = policies.iterator(); iterator.hasNext(); ) {
            RemovalPolicy policy = iterator.next();
            Configuration section = config.getConfigurationSection(pathPrefix + policy.getName());

            if (section != null) {
                boolean enabled = section.getBoolean("Enabled");

                if (!enabled) {
                    iterator.remove();
                    continue;
                }

                policy.load(section);
            }
        }
    }

    @Override
    public void load(Configuration config) {
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
