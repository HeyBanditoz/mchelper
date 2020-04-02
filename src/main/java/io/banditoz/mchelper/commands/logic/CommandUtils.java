package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
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
                l.error("Exception! Offending message: " + buildMessageAndAuthor(e), ex);
            }
            else {
                l.error("Uncaught exception! Offending message: " + buildMessageAndAuthor(e), ex);
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
        msg = formatMessage(msg);
        Queue<Message> toSend = new MessageBuilder()
                .append(msg)
                .buildAll(MessageBuilder.SplitPolicy.NEWLINE);
        toSend.forEach(message -> c.sendMessage(message).queue());
    }

    public static void sendImageReply(String msg, ByteArrayOutputStream image, MessageReceivedEvent e) throws Exception {
        String imageName = UUID.randomUUID().toString().replace("-", "") + ".png";
        File f = new File(imageName);

        // compress image to oxipng (https://github.com/shssoichiro/oxipng)
        try (OutputStream outputStream = new FileOutputStream(imageName)) {
            image.writeTo(outputStream);

            Process p = new ProcessBuilder("oxipng", imageName).start();
            p.waitFor();

            e.getMessage().getChannel()
                    .sendMessage(CommandUtils.formatMessage(msg))
                    .addFile(f)
                    .queue();
            image.close();
        }
        finally {
            f.delete();
        }
    }

    public static void sendEmbedImageReply(MessageEmbed me, ByteArrayOutputStream image, MessageReceivedEvent e) throws Exception {
        String imageName = UUID.randomUUID().toString().replace("-", "") + ".png";
        File f = new File(imageName);

        // compress image to oxipng (https://github.com/shssoichiro/oxipng)
        try (OutputStream outputStream = new FileOutputStream(imageName)) {
            image.writeTo(outputStream);

            Process p = new ProcessBuilder("oxipng", imageName).start();
            p.waitFor();

            e.getMessage().getChannel()
                    .sendMessage(me)
                    .addFile(f)
                    .queue();
            image.close();
        }
        finally {
            f.delete();
        }
    }

    public static String formatMessage(String msg) {
        if (msg == null) {
            msg = "<null output>";
        }
        else if (msg.isEmpty()) {
            msg = "<no output>";
        }
        return msg;
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

    private static String buildMessageAndAuthor(MessageReceivedEvent e) {
        return "<" + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + "> " + e.getMessage().getContentRaw();
    }

    public static void sendFile(String msg, File f, MessageReceivedEvent e) {
        e.getChannel().sendMessage(msg).addFile(f).queue();
    }
}
