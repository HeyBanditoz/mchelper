package io.banditoz.mchelper.database;

import net.dv8tion.jda.api.entities.User;

/**
 * Represents a user from the username cache.
 *
 * @param id            The user's snowflake ID. Unique!
 * @param username      The user's username. Not unique, as not all users/bots are on their
 *                      <a href="https://support.discord.com/hc/en-us/articles/12620128861463">Pomelo</a> (unique usernames) system.
 * @param discriminator The user's discriminator. 0 (null in the DB) when they're a Pomelo user. Bots don't have this yet (as of 2023-08-24.)
 * @param displayName   The user's chosen display name. Generally, this is what the Discord client displays.
 * @param isBot         Is this user a bot under Discord's lens?
 */
public record FakeUser(long id, String username, int discriminator, String displayName, boolean isBot) {
    /**
     * @param u The {@link User} to create into a {@link FakeUser}.
     * @return A new {@link FakeUser}.
     */
    public static FakeUser of(User u) {
        return new FakeUser(u.getIdLong(), u.getName(), Integer.parseInt(u.getDiscriminator()), u.getGlobalName(), u.isBot());
    }
}
