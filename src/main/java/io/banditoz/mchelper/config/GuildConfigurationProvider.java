package io.banditoz.mchelper.config;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.UserEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.SortedMap;

/**
 * The guild config provider. Scope it per event.
 */
public class GuildConfigurationProvider {
    private final Guild guild;
    private final User user;
    private final MCHelper mcHelper;
    private final ConfigurationProvider config;

    public GuildConfigurationProvider(UserEvent ue) {
        this.guild = ue.getGuild();
        this.user = ue.getUser();
        this.mcHelper = ue.getMCHelper();
        this.config = new ConfigurationProvider(ue.getDatabase());
    }

    public GuildConfigurationProvider(Guild guild, User user, MCHelper mcHelper) {
        this.guild = guild;
        this.user = user;
        this.mcHelper = mcHelper;
        this.config = new ConfigurationProvider(mcHelper.getDatabase());
    }

    public String get(Config c) {
        return config.getValue(c, guild.getIdLong());
    }

    /**
     * Returns all configs for a {@link Guild} in order sorted by the {@link Config} enum.
     * If a config value doesn't exist for the guild in the DB, it will add its default in the {@link SortedMap}.
     *
         * @return A map containing all {@link Config Configs.}
     */
    public SortedMap<Config, String> getAllConfigs() throws SQLException {
        return config.getAllConfigs(guild);
    }

    public void set(Config c, String value) throws SQLException {
        if (value.equals("null")) {
            config.writeValue(c, null, guild.getIdLong(), user.getIdLong());
        }
        else {
            switch (c) {
                case PREFIX -> {
                    if (value.length() > 1) {
                        throw new IllegalArgumentException("Expecting string of length 1, got " + value.length() + " instead.");
                    }
                }
                case DEFAULT_CHANNEL -> {
                    TextChannel channel = guild.getTextChannelById(value);
                    if (channel == null || !channel.getGuild().equals(guild)) {
                        throw new IllegalArgumentException("Could not find text channel by id " + value);
                    }
                    else if (!channel.canTalk()) {
                        throw new IllegalArgumentException("This bot cannot talk in that channel. Check permissions.");
                    }
                }
                case POST_QOTD_TO_DEFAULT_CHANNEL -> {
                    if (get(Config.DEFAULT_CHANNEL) == null) {
                        throw new IllegalArgumentException("No DEFAULT_CHANNEL set.");
                    }
                    Boolean.parseBoolean(value); // will throw exception if fails
                }
                case DADBOT_CHANCE, BETBOT_CHANCE -> {
                    checkWithinZeroAndOne(value);
                }
                case VOICE_ROLE_ID -> {
                    Role role = guild.getRoleById(value);
                    if (role == null || !role.getGuild().equals(guild)) {
                        throw new IllegalArgumentException("Could not find role by id " + value);
                    }
                    if (!guild.getMemberById(mcHelper.getJDA().getSelfUser().getIdLong()).hasPermission(Permission.MANAGE_ROLES)) {
                        throw new IllegalArgumentException("This bot needs MANAGE_ROLES to do this.");
                    }
                }
            }
            config.writeValue(c, value, guild.getIdLong(), user.getIdLong());
        }
    }

    private void checkWithinZeroAndOne(String s) {
        double d = Double.parseDouble(s);
        if (d > 1 || d < 0) {
            throw new IllegalArgumentException("Must be between 0 and 1, got " + d + " instead.");
        }
    }
}
