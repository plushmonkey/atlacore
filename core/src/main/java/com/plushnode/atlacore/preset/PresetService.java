package com.plushnode.atlacore.preset;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.platform.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PresetService {
    private Executor executor;
    private PresetRepository repository;

    public PresetService(PresetRepository repository) {
        this.repository = repository;
        this.executor = Executors.newFixedThreadPool(1);
    }

    public void createPreset(Player player, String presetName, Consumer<Preset> callback) {
        Preset preset = new Preset(player.getUniqueId(), presetName);

        List<AbilityDescription> abilities = new ArrayList<>();
        for (int i = 1; i < 10; ++i) {
            abilities.add(player.getSlotAbility(i));
        }
        preset.setAbilities(abilities);

        executor.execute(() -> {
            repository.savePreset(preset);

            Game.plugin.createTask(() -> callback.accept(preset), 0);
        });
    }

    public void loadPreset(Player player, String presetName, Consumer<Preset> callback) {
        executor.execute(() -> {
            Preset preset = repository.getPreset(player.getUniqueId(), presetName);

            Game.plugin.createTask(() -> callback.accept(preset), 0);
        });
    }

    public void getPresetNames(Player player, Consumer<List<String>> callback) {
        executor.execute(() -> {
            List<String> presetNames = repository.getPresetNames(player.getUniqueId());

            Game.plugin.createTask(() -> callback.accept(presetNames), 0);
        });
    }

    public void deletePreset(Player player, String presetName, Consumer<Boolean> callback) {
        executor.execute(() -> {
            boolean success = repository.deletePreset(player.getUniqueId(), presetName);

            Game.plugin.createTask(() -> callback.accept(success), 0);
        });
    }
}
