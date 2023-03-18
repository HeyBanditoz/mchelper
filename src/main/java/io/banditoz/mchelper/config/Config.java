package io.banditoz.mchelper.config;

public enum Config {
    PREFIX("!", "The prefix (character) to invoke bot commands."),
    DEFAULT_CHANNEL(null, "The channel to send join/leave events to."),
    POST_QOTD_TO_DEFAULT_CHANNEL("false", "Whether to post the QOTD to the default channel or not."),
    DADBOT_CHANCE("0.0", "The chance to invoke dad bot. 0 means 0%, 1 means 100%."),
    BETBOT_CHANCE("0.0", "The chance to invoke bet bot. 0 means 0%, 1 means 100%."),
    VOICE_ROLE_ID(null, "Which role to grant to users (not bots) joining a voice channel.");

    private final String defaultValue;
    private final String description;

    Config(String defaultValue, String description) {
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }
}
