package io.banditoz.mchelper.utils;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandUtils {
    private static final boolean SEND_FULL_STACK_TRACE = false;
    public static void sendExceptionMessage(MessageReceivedEvent event, Exception ex) {
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
            ex.printStackTrace();
        }
        event.getChannel().sendMessage(reply.toString()).queue();
    }
}
