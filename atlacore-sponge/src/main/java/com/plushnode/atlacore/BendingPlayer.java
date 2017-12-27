package com.plushnode.atlacore;

import org.spongepowered.api.data.key.Keys;

import java.util.Optional;

public class BendingPlayer extends BendingUser implements Player {
    public BendingPlayer(org.spongepowered.api.entity.living.player.Player player) {
        super(player);
    }

    public org.spongepowered.api.entity.living.player.Player getSpongePlayer() {
        return (org.spongepowered.api.entity.living.player.Player)entity;
    }

    @Override
    public boolean isOnline() {
        return getSpongePlayer().isOnline();
    }

    @Override
    public boolean isSneaking() {
        org.spongepowered.api.entity.living.player.Player player = getSpongePlayer();

        if (player.supports(Keys.IS_SNEAKING)) {
            Optional<Boolean> result = player.get(Keys.IS_SNEAKING);
            if (result.isPresent()) {
                return result.get();
            }
        }

        return false;
    }
}
