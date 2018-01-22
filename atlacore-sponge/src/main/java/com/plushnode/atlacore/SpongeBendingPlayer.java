package com.plushnode.atlacore;

import com.plushnode.atlacore.ability.AbilityDescription;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.entity.Hotbar;

import java.util.Optional;

public class SpongeBendingPlayer extends SpongeBendingUser implements Player {
    public SpongeBendingPlayer(org.spongepowered.api.entity.living.player.Player player) {
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

    @Override
    public AbilityDescription getSelectedAbility() {
        Hotbar hotbar = getSpongePlayer().getInventory().query(Hotbar.class);
        int slot = hotbar.getSelectedSlotIndex() + 1;
        return getSlotAbility(slot);
    }
}
