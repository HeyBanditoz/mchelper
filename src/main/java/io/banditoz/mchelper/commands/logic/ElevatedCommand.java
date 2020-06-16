package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Represents the abstract class for any command that only bot maintainers can run.
 *
 * @see Command
 */
public abstract class ElevatedCommand extends Command {
    @Override
    protected void tryToExecute(MessageReceivedEvent e) {
        if (CommandPermissions.isBotOwner(e.getAuthor())) {
            LOGGER.info(String.format("Executing elevated command: <%s@%s> %s",
                    e.getAuthor().toString(), e.getChannel().toString(), e.getMessage().getContentDisplay()));
            execute(e);
        }
        else {
            CommandUtils.sendReply(String.format("User <%s> does not have permission to run this command!",
                    e.getAuthor().toString()), e);
        }
    }
}
