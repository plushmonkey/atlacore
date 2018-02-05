package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaPlugin;
import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.BlockWrapper;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
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
    public void onBlockPlace(ChangeBlockEvent.Place event) {
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
