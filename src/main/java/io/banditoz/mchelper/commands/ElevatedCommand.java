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
            if (e.getJDA().getSelfUser().getId().equals(e.getAuthor().getId())) return; // don't execute own commands.
            initialize(e);
                if (CommandPermissions.isBotOwner(e.getAuthor())) {
                    this.e.getChannel().sendTyping().queue(); // TODO remove duplicated code here and in Command
                    Thread thread = new Thread(() -> {
                        try {
                            logger.info("Executing elevated command with args \"" + commandArgsString + "\" from user " + e.getAuthor().getName() + "...");
                            long before = System.nanoTime();
                            onCommand();
                            long after = System.nanoTime() - before;
                            logger.debug("Command ran in " + (after / 1000000) + " ms.");
                        } catch (Exception ex) {
                            sendExceptionMessage(ex, false);
                        }
                    });
                    thread.start();
                }
                else {
                    sendReply(String.format("User %s (ID: %s) does not have permission to run this command!", e.getAuthor().getAsTag(), e.getAuthor().getId()));
                }
        }
    }
}
