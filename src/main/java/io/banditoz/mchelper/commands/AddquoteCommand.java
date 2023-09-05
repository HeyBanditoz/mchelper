package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.EventHandler;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Requires(database = true)
public class AddquoteCommand extends Command {
    private QuotesDao qd;
    // hacky, surely there is a better way to do this instead of a silly cache in the same class?
    private final Cache<Long, CommandEventAndQuote> cache = new Cache2kBuilder<Long, CommandEventAndQuote>() {}
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .suppressExceptions(false)
            .build();

    private final EventHandler<MessageReceivedEvent> listener = new EventHandler<>() {
        private static final Pattern QUOTE_PARSER = Pattern.compile("^[\"“”](.*?)[\"“”]\\s+");

        @Override
        public void eventConsumer(GenericEvent event) {
            MessageReceivedEvent messageReceivedEvent = (MessageReceivedEvent) event;
            if (!events.containsKey(messageReceivedEvent.getAuthor().getId())) {
                return;
            }
            if (!QUOTE_PARSER.matcher(messageReceivedEvent.getMessage().getContentDisplay()).find()) {
                return;
            }
            events.get(messageReceivedEvent.getAuthor().getId()).accept(messageReceivedEvent);
        }
    };

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
        // hacky, fix this (maybe with DI?)
        if (qd == null) {
            qd = new QuotesDaoImpl(ce.getDatabase());
        }
        if (!ce.getMCHelper().getJDA().getRegisteredListeners().contains(listener)) {
            ce.getMCHelper().getJDA().addEventListener(listener);
        }

        QuotesDao qd = new QuotesDaoImpl(ce.getDatabase());
        int id;
        MessageCreateBuilder message;
        Button b = Button.primary(UUID.randomUUID().toString(), "✏️");

        if (Character.isDigit(ce.getCommandArgs()[1].charAt(0))) {
            long messageId = Long.parseLong(ce.getCommandArgs()[1]);
            if (ce.getCommandArgs()[1].length() != 18) {
                throw new NumberFormatException("Not the proper current snowflake length of 18!");
            }
            NamedQuote nq = NamedQuote.parseMessageId(messageId, (TextChannel) ce.getEvent().getChannel());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            id = qd.saveQuote(nq);
            message = new MessageCreateBuilder().setContent("Quote (from message ID) added.");
        }
        else {
            NamedQuote nq = NamedQuote.parseString(ce.getCommandArgsString());
            nq.setGuildId(ce.getGuild().getIdLong());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            id = qd.saveQuote(nq);
            message = new MessageCreateBuilder().setContent("Quote added.");
        }
        if (!cache.containsKey(ce.getEvent().getAuthor().getIdLong())) {
            message.addActionRow(b);
        }
        int finalId = id;
        ce.getEvent().getChannel().sendMessage(message.build()).queue(sentMessage -> {
            User u = ce.getEvent().getAuthor();
            if (!cache.containsKey(u.getIdLong())) {
                cache.put(u.getIdLong(), new CommandEventAndQuote(ce, finalId));
                ButtonInteractable bi = new ButtonInteractable(Map.of(b, this::editQuote),
                        user -> ce.getEvent().getMessage().getAuthor().equals(user),
                        60, sentMessage, ce);
                ce.getMCHelper().getButtonListener().addInteractable(bi);
            }
        });
        return Status.SUCCESS;
    }

    private void editQuote(WrappedButtonClickEvent event) {
        Message finalMessage = event.getMessage();
        MessageEditData edit = MessageEditBuilder.fromMessage(finalMessage)
                .setComponents(Collections.emptyList())
                .build();
        event.getEvent().editMessage(edit).queue();
        CommandEventAndQuote ceq = cache.get(event.getEvent().getUser().getIdLong());
        if (ceq == null) {
            event.removeListenerAndDestroy(event);
            return;
        }
        CommandEvent ce = ceq.ce();
        int finalId = ceq.id();
        LOGGER.info("{} is editing quote {}.", ceq.ce.getEvent().getAuthor(), finalId);

        ce.sendReply("Enter your new quote (\"<new_quote>\" <author>)");
        listener.addEvent(event.getEvent().getMember().getId(), messageEvent -> {
            try {
                NamedQuote namedQuote = NamedQuote.parseString(messageEvent.getMessage().getContentDisplay());
                qd.editQuote(finalId, namedQuote);
                ce.sendReply("Quote edited!");
                LOGGER.info("{} has edited quote {}.", ceq.ce.getEvent().getAuthor(), finalId);
            } catch (IllegalArgumentException ex) {
                return;
            } catch (SQLException ex) {
                ce.sendExceptionMessage(ex);
            }
            cache.remove(messageEvent.getAuthor().getIdLong());
            listener.removeEvent(messageEvent.getAuthor().getId());
            event.removeListener();
        });
        event.getMCHelper().getSES().schedule(() -> {
            if (listener.containsEvent(ce.getEvent().getAuthor().getId())) {
                cache.remove(finalMessage.getIdLong());
                listener.removeEvent(ce.getEvent().getAuthor().getId());
                ce.sendReply(finalMessage.getAuthor().getAsMention() + ", your quote edit has expired.");
            }
        },1, TimeUnit.MINUTES);
    }

    /** The poor man's tuple. */
    private record CommandEventAndQuote(CommandEvent ce, int id) {}
}