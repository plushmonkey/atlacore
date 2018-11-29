package com.plushnode.atlacore;

import com.plushnode.atlacore.event.EventBus;
import com.plushnode.atlacore.platform.ItemSnapshot;
import com.plushnode.atlacore.platform.PotionFactory;
import com.plushnode.atlacore.platform.block.BlockSetter;
import com.plushnode.atlacore.platform.ParticleEffectRenderer;
import com.plushnode.atlacore.platform.block.Material;
import com.plushnode.atlacore.platform.block.data.BlockData;
import com.plushnode.atlacore.player.PlayerFactory;
import com.plushnode.atlacore.util.Task;
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
    PotionFactory getPotionFactory();

    PlayerFactory getPlayerFactory();
    String getConfigFolder();
    void loadConfig();

    void info(String message);
    void warn(String message);

    EventBus getEventBus();

    BlockData createBlockData(Material material);

    ItemSnapshot getBottleSnapshot();
}
