package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.permissions.CommandPermissions;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Represents the abstract class for any command that only bot maintainers can run.
 *
 * @see io.banditoz.mchelper.commands.Command
 */
public abstract class ElevatedCommand extends Command {
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (containsCommand(e)) {
            if (CommandPermissions.isBotOwner(e.getAuthor())) {
                initialize(e);
                go();
            }
            else {
                sendReply(String.format("User %s (ID: %s) does not have permission to run this command!", e.getAuthor().getAsTag(), e.getAuthor().getId()));
            }
        }
    }
}
