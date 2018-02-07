package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaCorePlugin;
import com.plushnode.atlacore.block.TempBlock;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.BlockWrapper;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockListener implements Listener {
    private AtlaCorePlugin plugin;

    public BlockListener(AtlaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            BlockWrapper bw = new BlockWrapper(event.getIgnitingBlock());

            if (Game.getTempBlockService().isTempBlock(bw)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        BlockWrapper bw = new BlockWrapper(event.getIgnitingBlock());
        if (Game.getTempBlockService().isTempBlock(bw)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockWrapper bw = new BlockWrapper(event.getBlock());

        TempBlock tempBlock = Game.getTempBlockService().getTempBlock(bw);
        if (tempBlock != null) {
            // Stop tracking it, but don't reset it.
            Game.getTempBlockService().remove(tempBlock);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        BlockWrapper bw = new BlockWrapper(event.getBlock());

        if (Game.getTempBlockService().isTempBlock(bw)) {
            event.setCancelled(true);
        }
    }
}
