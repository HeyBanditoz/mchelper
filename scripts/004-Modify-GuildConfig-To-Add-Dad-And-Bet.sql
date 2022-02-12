ALTER TABLE guild_config ADD COLUMN `dadbot_chance` FLOAT AFTER post_qotd_to_default_channel;
ALTER TABLE guild_config ADD COLUMN `betbot_chance` FLOAT AFTER dadbot_chance;