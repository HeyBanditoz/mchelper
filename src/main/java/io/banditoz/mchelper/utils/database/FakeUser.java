package io.banditoz.mchelper.utils.database;

import net.dv8tion.jda.api.entities.User;

/**
 * Represents a user from the username cache.
 *
 * @param id       The user's snowflake ID. Unique!
 * @param username The user's username. Not unique.
 */
public record FakeUser(long id, String username) {
    /**
     * @param u The {@link User} to create into a {@link FakeUser}.
     * @return A new {@link FakeUser}.
     */
    public static FakeUser of(User u) {
        return new FakeUser(u.getIdLong(), u.getName());
    }
}
