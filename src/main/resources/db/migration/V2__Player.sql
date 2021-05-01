CREATE TABLE matchmaking_player (
    id VARCHAR(100) NOT NULL,
    version VARCHAR(100) NOT NULL,
    game_mode VARCHAR(100) NOT NULL,
    region VARCHAR(100) NOT NULL,
    status VARCHAR(100) NOT NULL,
    game_server_id VARCHAR(100) NULL,
    ticket VARCHAR(100) NULL,
    matched_time TIMESTAMP NULL,
    joined_time TIMESTAMP NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (game_server_id) REFERENCES matchmaking_gameserver(id)
);
