package io.banditoz.mchelper.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;

public class CommandUtils {
    private static final boolean SEND_FULL_STACK_TRACE = false;

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public static void sendExceptionMessage(MessageReceivedEvent e, Exception ex, Logger l, boolean caught) {
        StringBuilder reply = new StringBuilder("**Exception thrown:** " + ex.toString()); // bold for Discord, and code blocks
        if (SEND_FULL_STACK_TRACE) {
            reply.append("\n```");
            for (int i = 0; i < ex.getStackTrace().length; i++) {
                reply.append(ex.getStackTrace()[i]);
                reply.append("\n");
            }
            reply.append("```");
        }
        else {
            if (caught) {
                l.error("Exception!", ex);
            }
            else {
                l.error("Uncaught exception!", ex);
            }
        }
        e.getChannel().sendMessage(reply.toString()).queue();
    }
}
