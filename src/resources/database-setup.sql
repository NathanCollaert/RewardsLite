CREATE TABLE IF NOT EXISTS rl_player
(
    player_uuid CHAR(36) NOT NULL,
    last_known_name VARCHAR(16) NULL,
    playtime BIGINT UNSIGNED NOT NULL,
    afk_time BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (player_uuid)
);

CREATE TABLE IF NOT EXISTS rl_reward
(
    reward_uuid CHAR(36) NOT NULL,
    player_uuid CHAR(36) NOT NULL,
    time_left BIGINT(19) NOT NULL,
    redeemed INT UNSIGNED NOT NULL,
    pending INT UNSIGNED NOT NULL,
    active TINYINT NOT NULL DEFAULT 1,
    counted_previous TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (reward_uuid, player_uuid),
    CONSTRAINT rl_fk_reward_player_uuid
        FOREIGN KEY (player_uuid)
        REFERENCES rl_player(player_uuid)
        ON DELETE CASCADE
        ON UPDATE NO ACTION
);