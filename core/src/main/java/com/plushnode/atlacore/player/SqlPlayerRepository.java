package com.plushnode.atlacore.player;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.game.ability.AbilityDescription;
import com.plushnode.atlacore.game.element.Element;
import com.plushnode.atlacore.platform.Player;
import com.plushnode.atlacore.store.sql.DatabaseManager;
import com.plushnode.atlacore.store.sql.SqlUtil;
import com.plushnode.atlacore.store.sql.TransactionGuard;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class SqlPlayerRepository implements PlayerRepository {
    private final DatabaseManager manager;
    private final PlayerFactory factory;

    public SqlPlayerRepository(DatabaseManager manager, PlayerFactory factory) {
        this.manager = manager;
        this.factory = factory;
    }

    @Override
    public Player getPlayerByUUID(UUID uuid) {
        Player player = null;

        try (Connection connection = this.manager.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bending_players WHERE uuid=?")) {
                stmt.setBytes(1, SqlUtil.uuidToBytes(uuid));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        player = factory.createPlayer(uuid);
                    }
                }
             } catch (SQLException|IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (player != null) {
            player.getElements().addAll(this.loadElements(player));
            this.loadSlots(player);
        }

        return player;
    }

    @Override
    public Player getPlayerByName(String name) {
        Player player = null;

        try (Connection connection = this.manager.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bending_players WHERE name=?")) {
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        player = factory.createPlayer(name);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (player != null) {
            player.getElements().addAll(this.loadElements(player));
            this.loadSlots(player);
        }

        return player;
    }

    @Override
    public Player createPlayer(UUID uuid, String name) {
        Player player = getPlayerByUUID(uuid);

        // Player already exists in the database, so just return it.
        if (player != null) {
            return player;
        }

        player = factory.createPlayer(uuid);
        String playerName = player.getName();

        // Save player to the database.
        try (Connection connection = this.manager.getConnection();
            TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bending_players (uuid, name, creation_time) VALUES(?, ?, ?)")) {
                    stmt.setBytes(1, SqlUtil.uuidToBytes(uuid));
                    stmt.setString(2, playerName);
                    stmt.setTimestamp(3, new Timestamp(Calendar.getInstance().getTime().getTime()));

                    stmt.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }))
        {
            guard.run();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // pass
        }

        return player;
    }

    @Override
    public void savePlayer(Player player) {
        saveElements(player);
        saveSlots(player);
    }

    private void loadSlots(Player player) {
        try (Connection connection = this.manager.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT slot, ability FROM bending_players_slots WHERE player_uuid=?")) {
                stmt.setBytes(1, SqlUtil.uuidToBytes(player.getUniqueId()));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int slot = rs.getInt("slot");
                        String abilityName = rs.getString("ability");

                        AbilityDescription desc = Game.getAbilityRegistry().getAbilityByName(abilityName);

                        if (player.hasPermission("atla.ability." + desc.getName())) {
                            player.setSlotAbility(slot, desc);
                        }
                    }
                }
            } catch (SQLException|IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSlots(Player player) {
        try (Connection connection = this.manager.getConnection();
            TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                // Delete all of the player's current slots
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM bending_players_slots WHERE player_uuid=?")) {
                    stmt.setBytes(1, SqlUtil.uuidToBytes(player.getUniqueId()));

                    stmt.execute();
                } catch (SQLException|IOException e) {
                    e.printStackTrace();
                }

                // Save in all of the slots that player has right now.
                for (int slotIndex = 1; slotIndex <= 9; ++slotIndex) {
                    AbilityDescription desc = player.getSlotAbility(slotIndex);
                    if (desc == null) continue;

                    try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bending_players_slots (slot, player_uuid, ability) VALUES(?, ?, ?)")) {
                        stmt.setInt(1, slotIndex);
                        stmt.setBytes(2, SqlUtil.uuidToBytes(player.getUniqueId()));
                        stmt.setString(3, desc.getName());

                        stmt.execute();
                    } catch (SQLException|IOException e) {
                        e.printStackTrace();
                    }
                }
               return true;
            }))
        {
            guard.run();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // pass
        }
    }

    @Override
    public void saveSlot(Player player, int slotIndex) {
        try (Connection connection = this.manager.getConnection();
            TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                // Delete the player's current slot
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM bending_players_slots WHERE player_uuid=? AND slot=?")) {
                    stmt.setBytes(1, SqlUtil.uuidToBytes(player.getUniqueId()));
                    stmt.setInt(2, slotIndex);

                    stmt.execute();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }

                // Save the slot.
                AbilityDescription desc = player.getSlotAbility(slotIndex);
                if (desc == null) return true;

                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bending_players_slots (slot, player_uuid, ability) VALUES(?, ?, ?)")) {
                    stmt.setInt(1, slotIndex);
                    stmt.setBytes(2, SqlUtil.uuidToBytes(player.getUniqueId()));
                    stmt.setString(3, desc.getName());

                    stmt.execute();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }

                return true;
            }))
        {
            guard.run();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // pass
        }
    }

    // Load in the elements from the database for a player
    private List<Element> loadElements(Player player) {
        String elementSql = "SELECT e.name FROM bending_players_elements pe JOIN bending_elements e ON e.id = pe.element_id WHERE pe.player_uuid=?";

        List<Element> elements = new ArrayList<>();

        try (Connection connection = this.manager.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(elementSql)) {
                stmt.setBytes(1, SqlUtil.uuidToBytes(player.getUniqueId()));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String elementName = rs.getString("name");

                        Element element = Game.getElementRegistry().getElementByName(elementName);
                        if (element != null) {
                            elements.add(element);
                        }
                    }
                }
            } catch (SQLException|IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return elements;
    }

    @Override
    public void saveElements(Player player) {
        String elementSql = "INSERT INTO bending_players_elements (element_id, player_uuid) SELECT e.id, ? FROM bending_elements e WHERE e.name LIKE ?";

        try (Connection connection = this.manager.getConnection();
            TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                // Delete all of the player's previous elements
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM bending_players_elements WHERE player_uuid=?")) {
                    stmt.setBytes(1, SqlUtil.uuidToBytes(player.getUniqueId()));

                    stmt.execute();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }

                // Save in all of the elements that player has right now.
                for (Element element : player.getElements()) {
                    try (PreparedStatement stmt = connection.prepareStatement(elementSql)) {
                        stmt.setBytes(1, SqlUtil.uuidToBytes(player.getUniqueId()));
                        stmt.setString(2, element.getName());

                        stmt.execute();
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }))
        {
            guard.run();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // pass
        }
    }

    public void createElements(List<Element> elements) {
        List<Element> created = getAllElements();

        try (Connection connection = this.manager.getConnection();
            TransactionGuard<Boolean> guard = new TransactionGuard<>(connection, () -> {
                for (Element element : elements) {
                    if (created.contains(element)) continue;

                    try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO bending_elements(name) VALUES(?)")) {
                        stmt.setString(1, element.getName());
                        stmt.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }))
        {
            guard.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Element> getAllElements() {
        List<Element> elements = new ArrayList<>();

        try (Connection connection = this.manager.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT name FROM bending_elements")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String elementName = rs.getString("name");

                        Element element = Game.getElementRegistry().getElementByName(elementName);
                        if (element != null) {
                            elements.add(element);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return elements;
    }
}
