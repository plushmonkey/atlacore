package com.plushnode.atlacore;

import com.plushnode.atlacore.block.BlockSetter;
import com.plushnode.atlacore.config.Configuration;

public interface CorePlugin {
    void onConfigReload(Configuration config);

    BlockSetter getBlockSetter();
    BlockSetter getBlockSetter(BlockSetter.Flag... flags);

    // The task returned contains the cancel method.
    Task createTask(Task task, long delay);
    // The task returned contains the cancel method.
    Task createTaskTimer(Task task, long delay, long period);
}
