package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.permissions.CommandPermissions;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class ElevatedCommand extends Command {
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (containsCommand(e)) {
            if (e.getJDA().getSelfUser().getId().equals(e.getAuthor().getId())) return; // don't execute own commands.
            initialize(e);
            try {
                if (CommandPermissions.isBotOwner(e.getAuthor())) {
                    e.getChannel().sendTyping().queue();
                    long before = System.nanoTime();
                    onCommand();
                    long after = System.nanoTime() - before;
                    logger.info("Executing elevated command with args \"" + commandArgsString + "\" from user " + e.getAuthor().getName() + "...");
                    logger.debug("Command with class " + getClass().getCanonicalName() + " ran in " + (after / 1000000) + " ms.");
                }
                else {
                    sendReply(String.format("User %s (ID: %s) does not have permission to run this command!", e.getAuthor().getAsTag(), e.getAuthor().getId()));
                }
            } catch (Exception ex) {
                sendExceptionMessage(ex);
            }
        }
    }
}
