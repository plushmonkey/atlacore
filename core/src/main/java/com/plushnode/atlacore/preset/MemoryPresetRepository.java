package com.plushnode.atlacore.preset;

import java.util.*;

public class MemoryPresetRepository implements PresetRepository {
    private Map<UUID, Map<String, Preset>> presetMap = new HashMap<>();

    @Override
    public Preset getPreset(UUID playerUniqueId, String presetName) {
        Map<String, Preset> presets = presetMap.get(playerUniqueId);

        if (presets == null) {
            return null;
        }

        return presets.get(presetName);
    }

    @Override
    public List<String> getPresetNames(UUID playerUniqueId) {
        Map<String, Preset> presets = presetMap.get(playerUniqueId);

        if (presets == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(presets.keySet());
    }

    @Override
    public boolean savePreset(Preset preset) {
        presetMap.putIfAbsent(preset.getCreatorId(), new HashMap<>());
        presetMap.get(preset.getCreatorId()).put(preset.getName(), preset);
        return true;
    }

    @Override
    public boolean deletePreset(UUID playerUniqueId, String presetName) {
        Map<String, Preset> presets = presetMap.get(playerUniqueId);
        if (presets == null) {
            return false;
        }

        if (presets.containsKey(presetName)) {
            presets.remove(presetName);
            return true;
        }

        return false;
    }
}
