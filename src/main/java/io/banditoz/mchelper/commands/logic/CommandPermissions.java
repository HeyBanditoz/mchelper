package io.banditoz.mchelper.commands.logic;

import io.avaje.config.Config;
import net.dv8tion.jda.api.entities.User;

public class CommandPermissions {
    /**
     * Checks if the {@link User} is a bot owner per the <code>application.yml</code>.
     *
     * @param attempter The {@link User} who should be checked for bot owner privileges.
     * @return <code>true</code> if they're an owner, <code>false</code> if not.
     */
    public static boolean isBotOwner(User attempter) {
        return _isBotOwner(attempter.getIdLong());
    }

    /**
     * Checks if the passed user ID is a bot owner per the <code>application.yml</code>.
     *
     * @param attempter The user ID who should be checked for bot owner privileges.
     * @return <code>true</code> if they're an owner, <code>false</code> if not.
     */
    public static boolean isBotOwner(long attempter) {
        return _isBotOwner(attempter);
    }

    private static boolean _isBotOwner(long attempter) {
        return Config.list().ofLong("mchelper.owners").contains(attempter);
    }
}
