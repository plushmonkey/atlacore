package com.plushnode.atlacore.listeners;

import com.plushnode.atlacore.AtlaCorePlugin;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.BlockWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;

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
    public void onBlockForm(BlockFormEvent event) {
        BlockWrapper bw = new BlockWrapper(event.getBlock());

        if (Game.getTempBlockService().isTempBlock(bw)) {
            event.setCancelled(true);
        }
    }
}
