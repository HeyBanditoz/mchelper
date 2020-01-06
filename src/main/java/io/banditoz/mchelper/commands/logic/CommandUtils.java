package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {
    private static final boolean SEND_FULL_STACK_TRACE = false;

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public static void sendExceptionMessage(MessageReceivedEvent e, Exception ex, Logger l, boolean caught, boolean blocked) {
        StringBuilder reply = new StringBuilder("**Exception thrown:** " + (blocked ? "```" : "") + ex.toString() + (blocked ? "```" : "")); // ternary abuse out the wazoo
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

    /**
     * Sends a reply. Note if msg is empty, &lt;no output&gt; will be send instead.
     * @param msg The reply.
     * @param e The MessageReceivedEvent to reply to.
     */
    public static void sendReply(String msg, MessageReceivedEvent e) {
        _sendReply(msg, e.getChannel());
    }

    /**
     * Sends a reply. Note if msg is empty, &lt;no output&gt; will be send instead.
     * @param msg The reply.
     */
    public static void sendReply(String msg, TextChannel chan) {
        _sendReply(msg, chan);
    }

    /**
     * Internal method for sending a reply. Named differently to prevent infinite recursion.
     * @param msg The reply.
     * @param c The MessageChannel to send to.
     */
    private static void _sendReply(String msg, MessageChannel c) {
        if (msg == null) {
            c.sendMessage("<null output>").queue();
        }
        else if (msg.isEmpty()) {
            c.sendMessage("<no output>").queue();
        }
        else {
            Queue<Message> toSend = new MessageBuilder()
                    .append(msg)
                    .buildAll(MessageBuilder.SplitPolicy.NEWLINE);
            toSend.forEach(message -> c.sendMessage(message).queue());
        }
    }

    public static String[] commandArgs(String string) {
        String message = string.replaceAll("\\*\\*<.*>\\*\\*", "");
        List<String> matches = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\S+").matcher(message);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches.toArray(new String[0]);
    }

    public static String generateCommandArgsString(MessageReceivedEvent e) {
        StringBuilder commandArgsBuilder = new StringBuilder();
        String[] args = commandArgs(e.getMessage().getContentDisplay());
        for (int i = 1; i < args.length; i++) {
            if (i == args.length - 1) {
                commandArgsBuilder.append(args[i]);
            }
            else {
                commandArgsBuilder.append(args[i]).append(" ");
            }

        }
        return commandArgsBuilder.toString();
    }
}
