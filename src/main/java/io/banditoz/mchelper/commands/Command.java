package io.banditoz.mchelper.commands;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command extends ListenerAdapter {
    protected abstract void onCommand();
    public abstract String commandName();
    protected static final String REGEX = "\\S+";
    protected String commandArgsString;
    protected String[] commandArgs;
    protected MessageReceivedEvent e;
    protected Logger logger;

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (containsCommand(e)) {
            initialize(e);
            try {
                this.e.getChannel().sendTyping().queue();
                onCommand();
            } catch (Exception ex) {
                sendExceptionMessage(ex, false);
            }
        }
    }

    /**
     * Initialize variables.
     */
    protected void initialize(MessageReceivedEvent e) {
        StringBuilder commandArgsBuilder = new StringBuilder();
        this.e = e;
        this.commandArgs = commandArgs(e.getMessage());
        for (int i = 1; i < commandArgs(e.getMessage()).length; i++) {
            commandArgsBuilder.append(commandArgs(e.getMessage())[i]).append(" ");
        }
        commandArgsString = commandArgsBuilder.toString();
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex, boolean caught) {
        CommandUtils.sendExceptionMessage(this.e, ex, logger, caught);
    }

    /**
     * Sends a reply containing the exception message.
     * @param ex The exception.
     */
    public void sendExceptionMessage(Exception ex) {
        CommandUtils.sendExceptionMessage(this.e, ex, logger, true);
    }

    /**
     * Sends a reply.
     * @param msg The reply.
     */
    public void sendReply(String msg) {
        Queue<Message> toSend = new MessageBuilder()
                .append(msg)
                .buildAll(MessageBuilder.SplitPolicy.ANYWHERE);
        toSend.forEach(message -> this.e.getChannel().sendMessage(msg).queue());
    }

    /**
     * Sends an EmbedReply.
     * @param me The reply.
     */
    public void sendEmbedReply(MessageEmbed me) {
        e.getChannel().sendMessage(me).queue();
    }

    protected String[] commandArgs(String string) {
        String message = string.replaceAll("\\*\\*<.*>\\*\\*", "");
        List<String> matches = new ArrayList<>();

        Matcher matcher = Pattern.compile(REGEX).matcher(message);
        while (matcher.find()) {
            matches.add(matcher.group());
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
