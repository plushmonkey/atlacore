CREATE TABLE IF NOT EXISTS bending_players(
    uuid BINARY(16) PRIMARY KEY NOT NULL,
    name VARCHAR(16) NOT NULL,
    creation_time TIMESTAMP NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bending_elements(
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
    name VARCHAR(32) NOT NULL UNIQUE,
    UNIQUE(name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bending_players_elements (
    element_id INT UNSIGNED NOT NULL,
    player_uuid BINARY(16) NOT NULL,
    FOREIGN KEY(element_id) REFERENCES bending_elements(id) ON DELETE CASCADE,
    FOREIGN KEY(player_uuid) REFERENCES bending_players(uuid) ON DELETE CASCADE,
    PRIMARY KEY(element_id, player_uuid)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bending_players_slots (
    slot INT UNSIGNED NOT NULL,
    player_uuid BINARY(16) NOT NULL,
    ability VARCHAR(32) NOT NULL,
    FOREIGN KEY(player_uuid) REFERENCES bending_players(uuid) ON DELETE CASCADE,
    PRIMARY KEY(slot, player_uuid),
    INDEX uuid_i(player_uuid)
) ENGINE=InnoDB;
