package com.plushnode.atlacore.game.ability.sequence;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.ability.ActivationMethod;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SequenceService {
    private Map<User, Sequence> userSequences = new HashMap<>();
    private Map<AbilityDescription, Sequence> abilitySequences = new HashMap<>();

    public void start() {
        Game.plugin.createTaskTimer(this::update, 20, 20);
    }

    public void update() {
        for (Iterator<Map.Entry<User, Sequence>> iter = userSequences.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<User, Sequence> entry = iter.next();
            User user = entry.getKey();

            if (user instanceof Player && !((Player) user).isOnline()) {
                iter.remove();
            }
        }
    }

    public void clear() {
        userSequences.clear();
    }

    public void registerSequence(AbilityDescription description, Sequence sequence) {
        abilitySequences.put(description, sequence);
    }

    public void registerAction(User user, Action action) {
        AbilityDescription desc = user.getSelectedAbility();
        if (desc == null) return;

        Sequence userSequence = userSequences.getOrDefault(user, new Sequence());

        userSequence = userSequence.addAction(new AbilityAction(desc, action));

        userSequences.put(user, userSequence);

        for (Map.Entry<AbilityDescription, Sequence> entry : abilitySequences.entrySet()) {
            AbilityDescription sequenceDesc = entry.getKey();
            Sequence sequence = entry.getValue();

            if (userSequence.matches(sequence)) {
                if (!user.canBend(sequenceDesc)) continue;

                Ability ability = sequenceDesc.createAbility();

                if (ability.activate(user, ActivationMethod.Sequence)) {
                    Game.getAbilityInstanceManager().addAbility(user, ability);
                }

                userSequences.put(user, new Sequence());
            }
        }
    }
}
