package com.plushnode.atlacore.platform;

import com.plushnode.atlacore.game.ability.AbilityDescription;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.text.Text;

public class SpongeBendingPlayer extends SpongeBendingUser implements Player {
    private org.spongepowered.api.entity.living.player.User spongeUser;

    public SpongeBendingPlayer(org.spongepowered.api.entity.living.player.User spongeUser) {
        super(spongeUser.getPlayer().orElse(null));

        this.spongeUser = spongeUser;
    }

    public org.spongepowered.api.entity.living.player.Player getSpongePlayer() {
        return (org.spongepowered.api.entity.living.player.Player)this.getSpongeEntity();
    }

    @Override
    public org.spongepowered.api.entity.Entity getSpongeEntity() {
        org.spongepowered.api.entity.Entity newEntity = spongeUser.getPlayer()
                .orElse((org.spongepowered.api.entity.living.player.Player)super.getSpongeEntity());

        setSpongeEntity(newEntity);
        return newEntity;
    }

    @Override
    public boolean isOnline() {
        return getSpongePlayer().isOnline();
    }

    @Override
    public boolean isSneaking() {
        org.spongepowered.api.entity.living.player.Player player = getSpongePlayer();
        return player.get(Keys.IS_SNEAKING).orElse(false);
    }

    @Override
    public void setSneaking(boolean sneaking) {
        getSpongePlayer().offer(Keys.IS_SNEAKING, sneaking);
    }

    @Override
    public boolean isSprinting() {
        org.spongepowered.api.entity.living.player.Player player = getSpongePlayer();
        return player.get(Keys.IS_SPRINTING).orElse(false);
    }

    @Override
    public void setSprinting(boolean sprinting) {
        getSpongePlayer().offer(Keys.IS_SPRINTING, sprinting);
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

    @Override
    public Inventory getInventory() {
        return new InventoryWrapper(getSpongePlayer());
    }
}
