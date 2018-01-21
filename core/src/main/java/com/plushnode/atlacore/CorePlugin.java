package com.plushnode.atlacore;

import com.plushnode.atlacore.block.BlockSetter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public interface CorePlugin {
    CommentedConfigurationNode getCoreConfig();
    BlockSetter getBlockSetter();
    BlockSetter getBlockSetter(BlockSetter.Flag... flags);

    // The task returned contains the cancel method.
    Task createTask(Task task, long delay);
    // The task returned contains the cancel method.
    Task createTaskTimer(Task task, long delay, long period);

    ParticleEffectRenderer getParticleRenderer();
}
