package io.banditoz.mchelper.utils.database;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record Lottery(int id, long guildId, long channelId, BigDecimal limit, Timestamp drawAt, boolean complete) {
}
