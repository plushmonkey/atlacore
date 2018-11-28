package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.BlockWrapper;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.util.MaterialUtil;
import com.plushnode.atlacore.util.SpongeTypeUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class BlockListener {
    private AtlaPlugin plugin;

    public BlockListener(AtlaPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().stream().forEach((transaction) -> {
            Optional<Location<World>> result = transaction.getFinal().getLocation();

            if (result.isPresent()) {
                BlockWrapper bw = new BlockWrapper(result.get());
                TempBlock tempBlock = Game.getTempBlockService().getTempBlock(bw);

                if (tempBlock != null) {
                    Game.plugin.createTask(tempBlock::reset, 1);
                    transaction.setValid(false);
                }
            }
        });
    }

    @Listener
    public void onPlayerBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
        event.getTransactions().stream().forEach(transaction -> {
            Optional<Location<World>> result = transaction.getFinal().getLocation();

            if (result.isPresent()) {
                Material type = SpongeTypeUtil.adapt(transaction.getOriginal().getState().getType());

                if (MaterialUtil.isPlant(type)) {
                    com.plushnode.atlacore.platform.Player gamePlayer = Game.getPlayerService().getPlayerByUUID(player.getUniqueId());

                    if (gamePlayer != null) {
                        AbilityDescription desc = gamePlayer.getSelectedAbility();

                        if (desc != null && desc.canSourcePlant(gamePlayer)) {
                            transaction.setValid(false);
                            event.setCancelled(true);
                        }
                    }
                }
            }
        });
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @First Player player) {
        event.getTransactions().stream().forEach((transaction) -> {
            Optional<Location<World>> result = transaction.getFinal().getLocation();

            if (result.isPresent()) {
                BlockWrapper bw = new BlockWrapper(result.get());

                TempBlock tempBlock = Game.getTempBlockService().getTempBlock(bw);
                if (tempBlock != null) {
                    // Stop tracking it, but don't reset it.
                    Game.getTempBlockService().remove(tempBlock);
                }
            }
        });
    }

    @Listener
    public void onBlockFlow(ChangeBlockEvent.Place event, @First LocatableBlock from) {
        if (Game.getTempBlockService().isTempBlock(new BlockWrapper(from.getLocation()))) {
            event.setCancelled(true);
            return;
        }

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);

            if (location != null && Game.getTempBlockService().isTempBlock(new BlockWrapper(location))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // This is needed for 1.11.2
    @Listener
    public void onBlockFlow(ChangeBlockEvent.Place event, @First PluginContainer container) {
        if (!(container.getInstance().orElse(null) instanceof AtlaPlugin)) {
            return;
        }

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Location<World> original = transaction.getOriginal().getLocation().orElse(null);

            if (original != null && Game.getTempBlockService().isTempBlock(new BlockWrapper(original))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener
    public void onBlockTick(TickBlockEvent.Random event) {
        // Stop fire TempBlocks from spreading.
        if (event.getTargetBlock().getState().getType() == BlockTypes.FIRE) {
            Optional<Location<World>> result = event.getTargetBlock().getLocation();

            if (result.isPresent()) {
                BlockWrapper bw = new BlockWrapper(result.get());

                if (Game.getTempBlockService().isTempBlock(bw)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
