package io.banditoz.mchelper.telemetry;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

public final class MCHelperAttributes {
    public static final AttributeKey<String> MCHELPER_NAME = stringKey("mchelper.name");
    public static final AttributeKey<String> MCHELPER_STATUS = stringKey("mchelper.status");
    public static final AttributeKey<String> MCHELPER_KIND = stringKey("mchelper.kind");
    public static final AttributeKey<Long> MCHELPER_EXECUTION_TIME_MS = longKey("mchelper.execution_time_ms");

    public static final AttributeKey<Long> DISCORD_USER_ID = longKey("discord.user_id");
    public static final AttributeKey<Long> DISCORD_CHANNEL_ID = longKey("discord.channel_id");
    public static final AttributeKey<Long> DISCORD_GUILD_ID = longKey("discord.guild_id");
    public static final AttributeKey<String> DISCORD_REST_ACTION = stringKey("discord.rest_action");
    public static final AttributeKey<String> DISCORD_CALL = stringKey("discord.call");
    public static final AttributeKey<Long> DISCORD_ERROR_CODE = longKey("discord.error_code");

    private MCHelperAttributes() {
    }
}
