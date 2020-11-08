package io.banditoz.mchelper.commands;


import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.EventHandler;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AddquoteCommand extends Command {

    private final EventHandler<MessageReactionAddEvent> handler = new EventHandler<MessageReactionAddEvent>() {
        @Override
        public void eventConsumer(GenericEvent event) {
            MessageReactionAddEvent messageReactionAddEvent = (MessageReactionAddEvent)event;
            if (messageReactionAddEvent.getUser().isBot() || events.get(messageReactionAddEvent.getUserId()) == null) {
                return;
            }
            if (events.containsKey(messageReactionAddEvent.getUserId())) {
                events.get(messageReactionAddEvent.getUserId()).accept(messageReactionAddEvent);
            }
        }
    };
    private final EventHandler<GuildMessageReceivedEvent> listener = new EventHandler<GuildMessageReceivedEvent>() {
        private final Pattern QUOTE_PARSER = Pattern.compile("^\"(.*?)\"\\s+");

        @Override
        public void eventConsumer(GenericEvent event) {
            GuildMessageReceivedEvent guildMessageReceivedEvent = (GuildMessageReceivedEvent)event;
            if (!events.containsKey(guildMessageReceivedEvent.getAuthor().getId())) {
                return;
            }
            if (!QUOTE_PARSER.matcher(guildMessageReceivedEvent.getMessage().getContentDisplay()).find()) {
                return;
            }
            events.get(guildMessageReceivedEvent.getAuthor().getId()).accept(guildMessageReceivedEvent);
        }
    };

    private static final int timeoutSecs = 60;

    @Override
    public String commandName() {
        return "addquote";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("\"<quote>\" <author>")
                .withDescription("Adds a quote to the database.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (!ce.getMCHelper().getJDA().getRegisteredListeners().contains(handler)) {
            ce.getMCHelper().getJDA().addEventListener(handler);
        }
        if (!ce.getMCHelper().getJDA().getRegisteredListeners().contains(listener)) {
            ce.getMCHelper().getJDA().addEventListener(listener);
        }
        QuotesDao qd = new QuotesDaoImpl(ce.getDatabase());
        int id = 0;
        Message message = null;
        try {
            long messageId = Long.parseLong(ce.getCommandArgs()[1]);
            if (String.valueOf(messageId).length() != 18) { // TODO Optimize this check
                throw new NumberFormatException();
            }
            NamedQuote nq = NamedQuote.parseMessageId(messageId, ce.getEvent().getTextChannel());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            id = qd.saveQuote(nq);
            message = new MessageBuilder("Quote (from message ID) added.").build();
        } catch (NumberFormatException ex) { // it isn't an ID, let's continue
            NamedQuote nq = NamedQuote.parseString(ce.getCommandArgsString());
            nq.setGuildId(ce.getGuild().getIdLong());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            id = qd.saveQuote(nq);
            message = new MessageBuilder("Quote added.").build();
        }
        ce.getEvent().getChannel().sendMessage(message).queue();
        Message finalMessage = ce.getEvent().getMessage();
        finalMessage.addReaction("✏").queue();
        int finalId = id;
        handler.addEvent(ce.getEvent().getMember().getId(), new Consumer<MessageReactionAddEvent>() {
            private Future<?> timeout;
            private final Consumer<Void> success = s -> handler.removeEvent(finalMessage.getAuthor().getId());

            {
                timeout = finalMessage.clearReactions().queueAfter(timeoutSecs, TimeUnit.SECONDS, success, (Consumer<Throwable>) null);
            }

            @Override
            public void accept(MessageReactionAddEvent event) {
                if (!finalMessage.getId().equals(event.getMessageId())) {
                    return;
                }
                if (!event.getReactionEmote().getEmoji().equals("✏")) {
                    return;
                }
                if (!finalMessage.getAuthor().equals(event.getUser())) {
                    return;
                }
                finalMessage.clearReactions().queue();
                timeout.cancel(false);
                ce.sendReply("Enter your new quote (\"<new_quote>\" <author>)");
                handler.removeEvent(finalMessage.getAuthor().getId());
                listener.addEvent(ce.getEvent().getMember().getId(), new Consumer<GuildMessageReceivedEvent>() {
                    @Override
                    public void accept(GuildMessageReceivedEvent guildMessageReceivedEvent) {
                        try {
                            NamedQuote namedQuote = NamedQuote.parseString(guildMessageReceivedEvent.getMessage().getContentDisplay());
                            qd.editQuote(finalId,namedQuote);
                            ce.sendReply("Quote edited!");
                        } catch (IllegalArgumentException ex) {
                            return;
                        } catch (SQLException ex) {
                            ce.sendExceptionMessage(ex);
                        }
                        listener.removeEvent(guildMessageReceivedEvent.getAuthor().getId());
                    }
                });
                ce.getMCHelper().getSES().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (listener.containsEvent(ce.getEvent().getAuthor().getId())) {
                            listener.removeEvent(ce.getEvent().getAuthor().getId());
                            ce.sendReply(finalMessage.getAuthor().getAsMention() + " your quote edit has expired.");
                        }
                    }
                },5, TimeUnit.MINUTES);
            }
        });
        return Status.SUCCESS;
    }
}