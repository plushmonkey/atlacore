package com.plushnode.atlacore.protection.cache;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.block.Block;
import com.plushnode.atlacore.platform.User;
import com.plushnode.atlacore.platform.Location;
import com.plushnode.atlacore.util.Task;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class PerUserProtectionCache implements ProtectionCache {
    private Map<User, Map<Block, BlockCacheElement>> blockCache = new HashMap<>();
    private Task runnable = null;
    private double period;

    public PerUserProtectionCache() {
        reload();
    }

    public void reload() {
        //period = ConfigManager.getConfig().getDouble("Properties.RegionProtection.CacheBlockTime");
        period = 5000;

        if (runnable != null) {
            runnable.cancel();
        }

        runnable = () -> {
            for (Map<Block, BlockCacheElement> map : blockCache.values()) {
                Iterator<Map.Entry<Block, BlockCacheElement>> iter;

                for (iter = map.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry<Block, BlockCacheElement> entry = iter.next();
                    BlockCacheElement value = entry.getValue();

                    if (System.currentTimeMillis() - value.time > period) {
                        iter.remove();
                    }
                }
            }
        };

        runnable = Game.plugin.createTaskTimer(runnable, 0, (long)(period / 20.0));
    }

    @Override
    public Optional<Boolean> canBuild(User user, AbilityDescription abilityDescription, Location location) {
        Map<Block, BlockCacheElement> blockMap = getBlockMap(user);

        Block block = location.getBlock();
        BlockCacheElement cacheElement = blockMap.get(block);

        if (cacheElement == null) {
            return Optional.empty();
        }

        return Optional.of(cacheElement.allowed);
    }

    @Override
    public void store(User user, AbilityDescription abilityDescription, Location location, boolean allowed) {
        Map<Block, BlockCacheElement> blockMap = getBlockMap(user);

        Block block = location.getBlock();
        blockMap.put(block, new BlockCacheElement(allowed, System.currentTimeMillis()));
    }

    private Map<Block, BlockCacheElement> getBlockMap(User user) {
        Map<Block, BlockCacheElement> blockMap = blockCache.get(user);

        if (blockMap == null) {
            blockMap = new HashMap<>();
            blockCache.put(user, blockMap);
        }

        return blockMap;
    }

    private class BlockCacheElement {
        boolean allowed;
        long time;

        BlockCacheElement(boolean allowed, long time) {
            this.allowed = allowed;
            this.time = time;
        }
    }
}
