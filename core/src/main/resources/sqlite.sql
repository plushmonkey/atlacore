CREATE TABLE IF NOT EXISTS bending_players(
    uuid BINARY(16) PRIMARY KEY NOT NULL,
    name VARCHAR(16) NOT NULL UNIQUE,
    creation_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS bending_elements(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(32) NOT NULL UNIQUE,
    UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS bending_players_elements (
    element_id INTEGER NOT NULL,
    player_uuid BINARY(16) NOT NULL,
    FOREIGN KEY(element_id) REFERENCES bending_elements(id) ON DELETE CASCADE,
    FOREIGN KEY(player_uuid) REFERENCES bending_players(uuid) ON DELETE CASCADE,
    PRIMARY KEY(element_id, player_uuid)
);

CREATE TABLE IF NOT EXISTS bending_players_slots (
    slot INTEGER NOT NULL,
    player_uuid BINARY(16) NOT NULL,
    ability VARCHAR(32) NOT NULL,
    FOREIGN KEY(player_uuid) REFERENCES bending_players(uuid) ON DELETE CASCADE,
    PRIMARY KEY(slot, player_uuid)
);

CREATE INDEX IF NOT EXISTS uuid_i ON bending_players_slots(player_uuid);