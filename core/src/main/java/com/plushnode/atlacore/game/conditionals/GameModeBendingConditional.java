package com.plushnode.atlacore.game.conditionals;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.GameMode;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.platform.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Prevents bending when in certain game modes.
public class GameModeBendingConditional implements BendingConditional {
    private List<GameMode> restricted = new ArrayList<>();

    public GameModeBendingConditional(GameMode... restricted) {
        this.restricted = Arrays.asList(restricted);
    }

    @Override
    public boolean canBend(User user, AbilityDescription desc) {
        if (!(user instanceof Player)) return true;

        GameMode gm = ((Player) user).getGameMode();

        return !restricted.contains(gm);
    }
}
