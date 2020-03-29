package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the abstract class for any command that only bot maintainers can run.
 *
 * @see Command
 */
public abstract class ElevatedCommand extends Command {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (containsCommand(e)) {
            if (CommandPermissions.isBotOwner(e.getAuthor())) {
                go(e);
            }
            else {
                CommandUtils.sendReply(String.format("User %s (ID: %s) does not have permission to run this command!", e.getAuthor().getAsTag(), e.getAuthor().getId()), e);
            }
        }
    }
}
