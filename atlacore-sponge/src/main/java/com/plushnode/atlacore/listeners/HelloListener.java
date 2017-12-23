package com.plushnode.atlacore.listeners;

import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class HelloListener {
    private Logger log;

    public HelloListener(Logger log) {
        this.log = log;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().forEach((transaction) -> {
            BlockSnapshot original = transaction.getOriginal();

            log.info("Breaking block " + original + ". Creating " + transaction.getFinal());
        });
    }
}
