package io.banditoz.mchelper.commands;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command extends ListenerAdapter {
    protected abstract void onCommand(MessageReceivedEvent e, String[] commandArgs);
    public abstract String commandName();
    protected static final boolean SEND_FULL_STACK_TRACE = false;
    protected static final String REGEX = "\\S+";

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (containsCommand(e)) {
            try {
                e.getChannel().sendTyping().queue();
                onCommand(e, commandArgs(e.getMessage()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Sends a reply containing the exception message. Use CommandUtils#sendExceptionMessage instead.
     * @param event The MessageReceivedEvent to reply to.
     * @param ex The exception.
     */
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

    /**
     * Sends a reply.
     * @param e The MessageReceivedEvent to reply to.
     * @param msg The reply.
     */
    public void sendReply(MessageReceivedEvent e, String msg) {
        Queue<Message> toSend = new MessageBuilder()
                .append(msg)
                .buildAll(MessageBuilder.SplitPolicy.ANYWHERE);
        toSend.forEach(message -> e.getChannel().sendMessage(msg).queue());
    }

    /**
     * Sends an EmbedReply.
     * @param e The MessageReceivedEvent to reply to.
     * @param me The reply.
     */
    public void sendEmbedReply(MessageReceivedEvent e, MessageEmbed me) {
        e.getChannel().sendMessage(me).queue();
    }

    protected String[] commandArgs(String string) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = Pattern.compile(REGEX).matcher(string);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        if (string.contains("**<")) {
            matches.remove(0);
        }
        return matches.toArray(new String[0]);
    }

    protected String[] commandArgs(Message message) {
        return commandArgs(message.getContentDisplay());
    }

    protected boolean containsCommand(MessageReceivedEvent e) {
        return commandName().equalsIgnoreCase(commandArgs(e.getMessage())[0]);
    }
}
