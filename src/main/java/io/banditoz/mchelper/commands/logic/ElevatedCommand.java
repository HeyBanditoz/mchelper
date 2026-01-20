package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.entities.User;

/**
 * Represents the abstract class for any command that only bot maintainers can run.
 *
 * @see Command
 */
public abstract class ElevatedCommand extends Command {
    @Override
    public boolean canExecute(User user) {
        return CommandPermissions.isBotOwner(user);
    }
}
