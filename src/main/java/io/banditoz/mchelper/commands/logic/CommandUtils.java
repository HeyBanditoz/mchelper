package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.utils.StringUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {
    private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);

    /**
     * Sends a reply containing the exception message.
     *
     * @param ex The exception.
     */
    public static void sendExceptionMessage(MessageReceivedEvent e, Exception ex, Logger l) {
        l.error("Exception! Offending message: " + buildMessageAndAuthor(e), ex);
        String reply = "**Status: Calamitous:** " + StringUtils.truncate(MarkdownSanitizer.escape(ex.toString()), 500, true);
        if (ex instanceof ArrayIndexOutOfBoundsException) {
            reply += " (are you missing arguments?)";
        }
        _sendReply(reply, e, true, true);
    }

    public static void sendThrowableMessage(MessageReceivedEvent e, Throwable t, Logger l) {
        l.error("THROWABLE! Offending message: " + buildMessageAndAuthor(e), t);
        String reply = "***STATUS: CALAMITOUS!!!*** " + StringUtils.truncate(MarkdownSanitizer.escape(t.toString()), 500, true);
        _sendReply(reply, e, true, true);
    }

    /**
     * Sends a reply. Note if msg is empty, &lt;no output&gt; will be send instead. All mentions will be sanitized, they
     * will appear as normal but, otherwise not do anything.
     *
     * @param msg The reply.
     * @param e   The MessageReceivedEvent to reply to.
     */
    public static void sendReply(String msg, MessageReceivedEvent e) {
        _sendReply(msg, e, true, true);
    }

    /**
     * Sends a reply. Note if msg is empty, &lt;no output&gt; will be send instead. All mentions will be sanitized, they
     * will appear as normal but, otherwise not do anything. The invoking user will <i>not</i> be pinged.
     *
     * @param msg The reply.
     * @param e   The MessageReceivedEvent to reply to.
     */
    public static void sendReplyWithoutPing(String msg, MessageReceivedEvent e) {
        _sendReply(msg, e, true, false);
    }

    /**
     * Sends a reply. Note if msg is empty, &lt;no output&gt; will be send instead. All mentions will <b>NOT</b> be
     * sanitized, all mentions will function as normal.
     *
     * @param msg The reply.
     * @param e   The MessageReceivedEvent to reply to.
     */
    public static void sendUnsanitizedReply(String msg, MessageReceivedEvent e) {
        _sendReply(msg, e, false, true);
    }

    /**
     * Internal method for sending a reply. Named differently to prevent infinite recursion.
     *
     * @param msg              The reply.
     * @param c                The MessageChannel to send to.
     * @param sanitizeMentions Whether or not to sanitize mentions contained in the reply.
     * @param ping             Whether or not to ping the invoker.
     */
    private static void _sendReply(String msg, MessageReceivedEvent c, boolean sanitizeMentions, boolean ping) {
        msg = formatMessage(msg);
        MessageBuilder mb = new MessageBuilder(msg);
        if (sanitizeMentions) {
            mb.denyMentions(Message.MentionType.values());
        }
        Queue<Message> toSend = mb.buildAll(MessageBuilder.SplitPolicy.NEWLINE);
        toSend.forEach(message -> c.getMessage().reply(message).mentionRepliedUser(ping).queue());
    }

    public static void sendImageReply(String msg, ByteArrayOutputStream image, MessageReceivedEvent e, boolean sanitizeMentions) throws Exception {
        String imageName = UUID.randomUUID().toString().replace("-", "") + ".png";
        File f = new File(imageName);

        // compress image to oxipng (https://github.com/shssoichiro/oxipng)
        try (OutputStream outputStream = new FileOutputStream(imageName)) {
            image.writeTo(outputStream);

            Process p = new ProcessBuilder("oxipng", imageName).start();
            p.waitFor();

            MessageBuilder m = new MessageBuilder(formatMessage(msg));
            if (sanitizeMentions) {
                m.denyMentions(Message.MentionType.values());
            }

            e.getMessage().getChannel()
                    .sendMessage(m.build())
                    .addFile(f)
                    .queue();
            image.close();
        } catch (IOException ex) {
            LOGGER.warn("There was most likely an error trying to execute oxipng: " + ex.getMessage());
            MessageBuilder m = new MessageBuilder(formatMessage(msg));
            if (sanitizeMentions) {
                m.denyMentions(Message.MentionType.values());
            }

            e.getMessage().getChannel()
                    .sendMessage(m.build())
                    .addFile(f)
                    .queue();
            image.close();
        } finally {
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
                    .sendMessageEmbeds(me)
                    .addFile(f)
                    .queue();
            image.close();
        } catch (IOException ex) {
            LOGGER.warn("There was most likely an error trying to execute oxipng: " + ex.getMessage());

            e.getMessage().getChannel()
                    .sendMessageEmbeds(me)
                    .addFile(f)
                    .queue();
            image.close();
        } finally {
            f.delete();
        }
    }

    public static void sendEmbedThumbnailReply(MessageEmbed me, ByteArrayOutputStream image, String randomUUID, MessageReceivedEvent e) throws Exception {
        String imageName = randomUUID + ".png";
        File f = new File(imageName);

        try (OutputStream outputStream = new FileOutputStream(imageName)) {
            image.writeTo(outputStream);

            Process p = new ProcessBuilder("oxipng", imageName).start();
            p.waitFor();

            e.getMessage().getChannel()
                    .sendFile(f).setEmbeds(me)
                    .queue();
            image.close();
        } catch (IOException ex) {
            LOGGER.warn("There was most likely an error trying to execute oxipng: " + ex.getMessage());

            e.getMessage().getChannel()
                    .sendMessageEmbeds(me)
                    .addFile(f)
                    .queue();
            image.close();
        } finally {
            f.delete();
        }
    }

    /**
     * Checks the message for abnormalities, (null, empty) and formats it accordingly. It will also take any URLs and
     * wrap them in brackets, so Discord doesn't embed them.
     *
     * @param msg The string to check.
     * @return The formatted message.
     */
    public static String formatMessage(String msg) {
        if (msg == null) {
            msg = "<null output>";
        }
        else if (msg.isEmpty()) {
            msg = "<no output>";
        }
        return URL_PATTERN.matcher(msg).replaceAll("<$0>"); // wrap String in brackets so discord won't embed it
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

    public static String generateCommandArgsString(MessageReceivedEvent e, boolean raw) {
        StringBuilder commandArgsBuilder = new StringBuilder();
        String[] args = commandArgs(raw ? e.getMessage().getContentRaw() : e.getMessage().getContentDisplay());
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
        return "<" + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + "> " + formatMessage(e.getMessage().getContentRaw());
    }

    public static void sendFile(String msg, File f, MessageReceivedEvent e) {
        e.getChannel().sendMessage(formatMessage(msg)).addFile(f).queue();
    }
}
