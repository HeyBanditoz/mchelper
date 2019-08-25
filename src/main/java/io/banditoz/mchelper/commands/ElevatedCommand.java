package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.permissions.CommandPermissions;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class ElevatedCommand extends Command {
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (containsCommand(e)) {
            try {
                if (CommandPermissions.isBotOwner(e.getAuthor())) {
                    e.getChannel().sendTyping().queue();
                    onCommand(e, commandArgs(e.getMessage()));
                }
                else {
                    sendReply(e, String.format("User %s (ID: %s) does not have permission to run this command!", e.getAuthor().getAsTag(), e.getAuthor().getId()));
                }
            } catch (Exception ex) {
                sendExceptionMessage(e, ex);
            }
        }
    }
}
