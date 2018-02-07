package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.text.Text;

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
    public GameMode getGameMode() {
        org.spongepowered.api.entity.living.player.gamemode.GameMode gm = getSpongePlayer().gameMode().get();

        if (gm == GameModes.ADVENTURE) {
            return GameMode.ADVENTURE;
        } else if (gm == GameModes.CREATIVE) {
            return GameMode.CREATIVE;
        } else if (gm == GameModes.SPECTATOR) {
            return GameMode.SPECTATOR;
        }

        return GameMode.SURVIVAL;
    }

    @Override
    public int getHeldItemSlot() {
        Hotbar hotbar = getSpongePlayer().getInventory().query(Hotbar.class);
        return hotbar.getSelectedSlotIndex();
    }

    @Override
    public AbilityDescription getSelectedAbility() {
        Hotbar hotbar = getSpongePlayer().getInventory().query(Hotbar.class);
        int slot = hotbar.getSelectedSlotIndex() + 1;
        return getSlotAbility(slot);
    }

    @Override
    public void sendMessage(String message) {
        getSpongePlayer().sendMessage(Text.of(message));
    }

    @Override
    public String getName() {
        return getSpongePlayer().getName();
    }

    @Override
    public boolean getAllowFlight() {
        return getSpongePlayer().get(Keys.CAN_FLY).orElse(false);
    }

    @Override
    public boolean isFlying() {
        return getSpongePlayer().get(Keys.IS_FLYING).orElse(false);
    }

    @Override
    public void setAllowFlight(boolean allow) {
        getSpongePlayer().offer(Keys.CAN_FLY, allow);
    }

    @Override
    public void setFlying(boolean flying) {
        getSpongePlayer().offer(Keys.IS_FLYING, flying);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getSpongePlayer().hasPermission(permission);
    }
}
