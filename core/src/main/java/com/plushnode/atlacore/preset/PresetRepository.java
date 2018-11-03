package com.plushnode.atlacore.preset;

import java.util.List;
import java.util.UUID;

public interface PresetRepository {
    Preset getPreset(UUID playerUniqueId, String presetName);
    List<String> getPresetNames(UUID playerUniqueId);

    boolean savePreset(Preset preset);
    boolean deletePreset(UUID playerUniqueId, String presetName);
}
