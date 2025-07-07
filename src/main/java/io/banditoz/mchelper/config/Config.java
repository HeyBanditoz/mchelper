package io.banditoz.mchelper.config;

public enum Config {
    PREFIX("!", "The prefix (character) to invoke bot commands."),
    DEFAULT_CHANNEL(null, "The channel to send join/leave events to."),
    POST_QOTD_TO_DEFAULT_CHANNEL("false", "Whether to post the QOTD to the default channel or not."),
    DADBOT_CHANCE("0.0", "The chance to invoke dad bot. 0 means 0%, 1 means 100%."),
    BETBOT_CHANCE("0.0", "The chance to invoke bet bot. 0 means 0%, 1 means 100%."),
    VOICE_ROLE_ID(null, "Which role to grant to users (not bots) joining a voice channel."),
    RSS_URLS("", "Space-separated list of RSS feeds to use in MOTD generation.", true),
    WEATHER_DEFAULT_LOC(null, "Default location to use in weather-related commands, and in MOTD generation."),
    BETTER_REDDIT_LINKS("false", "If reddit links should be sent using rxddit instead, for better embeds."),
    BETTER_TWITTER_LINKS("false", "If Twitter links should be sent using vxtwitter instead, for better embeds."),
    LISTEN_FOR_SCRYFALL("false", "If the bot should listen for MTG cards and send info about them, example: [[Commander Sphere]]");

    /** The default value this config should return, when no value is set. */
    private final String defaultValue;
    /** What does this config do? */
    private final String description;
    /** Whether the config should only be editable by bot owners. */
    private final boolean botOwnerLocked;

    Config(String defaultValue, String description) {
        this.defaultValue = defaultValue;
        this.description = description;
        this.botOwnerLocked = false;
    }

    Config(String defaultValue, String description, boolean botOwnerLocked) {
        this.defaultValue = defaultValue;
        this.description = description;
        this.botOwnerLocked = botOwnerLocked;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBotOwnerLocked() {
        return botOwnerLocked;
    }
}
