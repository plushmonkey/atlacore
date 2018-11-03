package com.plushnode.atlacore.preset;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.store.sql.DatabaseManager;
import com.plushnode.atlacore.store.sql.SqlUtil;
import com.plushnode.atlacore.store.sql.TransactionGuard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlPresetRepository implements PresetRepository {
    private final DatabaseManager manager;

    public SqlPresetRepository(DatabaseManager databaseManager) {
        this.manager = databaseManager;
    }

    @Override
    public Preset getPreset(UUID playerUniqueId, String presetName) {
        Preset preset = null;

        try (Connection connection = this.manager.getConnection()) {
            int presetId = getPresetId(connection, playerUniqueId, presetName);

            if (presetId >= 0) {
                try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bending_presets_slots WHERE preset_id=?")) {
                    stmt.setInt(1, presetId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        preset = new Preset(playerUniqueId, presetName);

                        List<AbilityDescription> abilities = new ArrayList<>();

                        for (int i = 0; i < 9; ++i) {
                            abilities.add(null);
                        }

                        while (rs.next()) {
                            int slot = rs.getInt(2);
                            String abilityName = rs.getString(3);

                            AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName(abilityName);
                            abilities.set(slot - 1, desc);
                        }

                        preset.setAbilities(abilities);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return preset;
    }

    @Override
    public List<String> getPresetNames(UUID playerUniqueId) {
        List<String> presetNames = new ArrayList<>();

        try (Connection connection = this.manager.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bending_presets where player_uuid=?");

            stmt.setBytes(1, SqlUtil.uuidToBytes(playerUniqueId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String presetName = rs.getString(3);
                    presetNames.add(presetName);
                }
            }
        } catch (SQLException|IOException e) {
            e.printStackTrace();
        }

        return presetNames;
    }

    @Override
    public boolean savePreset(Preset preset) {
        deletePreset(preset.getCreatorId(), preset.getName());

        try (Connection connection = this.manager.getConnection()) {
            try (TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bending_presets (player_uuid, name) VALUES(?, ?)")) {
                    stmt.setBytes(1, SqlUtil.uuidToBytes(preset.getCreatorId()));
                    stmt.setString(2, preset.getName());

                    stmt.execute();
                } catch (SQLException|IOException e) {
                    e.printStackTrace();
                }

                return true;
            })) {
                guard.run();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                // pass
            }

            int presetId = getPresetId(connection, preset.getCreatorId(), preset.getName());

            if (presetId < 0) {
                return false;
            }

            try (TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                for (int slotIndex = 1; slotIndex <= 9; ++slotIndex) {
                    AbilityDescription desc = preset.getAbilities().get(slotIndex - 1);

                    if (desc != null) {
                        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bending_presets_slots (preset_id, slot, ability) VALUES(?, ?, ?)")) {
                            stmt.setInt(1, presetId);
                            stmt.setInt(2, slotIndex);
                            stmt.setString(3, desc.getName());

                            stmt.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return true;
            })) {
                guard.run();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                // pass
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean deletePreset(UUID playerUniqueId, String presetName) {
        try (Connection connection = this.manager.getConnection()) {
            int presetId = getPresetId(connection, playerUniqueId, presetName);

            if (presetId > 0) {
                try (TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                    try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM bending_presets_slots WHERE preset_id=?")) {
                        stmt.setInt(1, presetId);

                        stmt.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }

                    try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM bending_presets WHERE id=?")) {
                        stmt.setInt(1, presetId);

                        stmt.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }

                    return true;
                })) {
                    guard.run();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                } catch (Exception e) {
                    // pass
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private int getPresetId(Connection connection, UUID playerUniqueId, String presetName) {
        int presetId = -1;

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bending_presets WHERE player_uuid=? AND name=?")) {
            stmt.setBytes(1, SqlUtil.uuidToBytes(playerUniqueId));
            stmt.setString(2, presetName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    presetId = rs.getInt(1);
                }
            }
        } catch (SQLException|IOException e) {
            e.printStackTrace();
        }

        return presetId;
    }
}
