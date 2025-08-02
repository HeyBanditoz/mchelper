package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Represents the abstract class for any command that only bot maintainers can run.
 *
 * @see Command
 */
public abstract class ElevatedCommand extends Command {
    @Override
    protected boolean canExecute(MessageReceivedEvent e) {
        if (CommandPermissions.isBotOwner(e.getAuthor())) {
            return true;
        }
        else {
            CommandUtils.sendReply(String.format("User <%s> does not have permission to run this command!",
                    e.getAuthor().toString()), e);
            return false;
        }
    }
}
