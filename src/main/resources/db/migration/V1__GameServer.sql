CREATE TABLE matchmaking_gameserver (
    id VARCHAR(100) NOT NULL,
    version VARCHAR(100) NOT NULL,
    game_mode VARCHAR(100) NOT NULL,
    region VARCHAR(100) NOT NULL,
    ipv4address VARCHAR(100) NOT NULL,
    port INT(10) UNSIGNED NOT NULL,
    max_players INT(3) UNSIGNED NOT NULL,
    last_heartbeat TIMESTAMP NOT NULL,
    status VARCHAR(100) NOT NULL,

    PRIMARY KEY (id)
);
