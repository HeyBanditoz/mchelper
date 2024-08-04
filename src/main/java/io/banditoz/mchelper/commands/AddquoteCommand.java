package io.banditoz.mchelper.commands;

import io.avaje.config.Config;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.interactions.ModalInteractable;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.SnowflakeUtils;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.NamedQuote.Flag;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

@Requires(database = true)
public class AddquoteCommand extends Command {
    private QuotesDao qd;

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

        QuotesDao qd = new QuotesDaoImpl(ce.getDatabase());
        int id;
        MessageCreateBuilder message;
        Button b = Button.primary(UUID.randomUUID().toString(), "✏️");

        NamedQuote nq;
        if (SnowflakeUtils.isLong(ce.getCommandArgsString())) {
            long messageId = Long.parseLong(ce.getCommandArgsString());
            nq = NamedQuote.parseMessageId(messageId, (TextChannel) ce.getEvent().getChannel());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            id = qd.saveQuote(nq, calculateRequiredFlags(nq));
            message = new MessageCreateBuilder().setContent("Quote (from message ID) added.");
        }
        else {
            nq = NamedQuote.parseString(ce.getCommandArgsString());
            nq.setGuildId(ce.getGuild().getIdLong());
            nq.setAuthorId(ce.getEvent().getAuthor().getIdLong());
            id = qd.saveQuote(nq, calculateRequiredFlags(nq));
            message = new MessageCreateBuilder()
                    .addActionRow(b)
                    .setContent("Quote added.");
        }
        nq.setId(id);
        ce.getEvent().getChannel().sendMessage(message.build()).queue(sentMessage -> {
            ButtonInteractable bi = new ButtonInteractable(Map.of(b, a -> editQuote(a, nq, ce.getMCHelper().getInteractionListener())),
                    user -> ce.getEvent().getMessage().getAuthor().equals(user),
                    60, sentMessage, ce);
            ce.getMCHelper().getInteractionListener().addInteractable(bi);
        });
        return Status.SUCCESS;
    }

    private void editQuote(WrappedButtonClickEvent event, NamedQuote nq, InteractionListener il) {
        String modalId = UUID.randomUUID().toString();
        TextInput quote = TextInput.create("quote", "Quote", TextInputStyle.PARAGRAPH)
                .setPlaceholder("New quote")
                .setMaxLength(1500)
                .setValue(nq.getQuote())
                .build();
        TextInput quoteAuthor = TextInput.create("quoteAuthor", "Quote Author", TextInputStyle.SHORT)
                .setPlaceholder("New quote author")
                .setMaxLength(100)
                .setValue(nq.getQuoteAuthor())
                .build();
        Modal modal = Modal.create("quoteEdit", "Quote Edit")
                .setId(modalId)
                .addComponents(ActionRow.of(quote), ActionRow.of(quoteAuthor))
                .build();
        event.getEvent().replyModal(modal).queue();
        ModalInteractable interactable = new ModalInteractable.Builder()
                .setModalId(modalId)
                .setCanInteract(event.getUser()::equals)
                .setTimeoutSeconds(600)
                .setConsumer(ee -> {
                    nq.editContent(
                            ee.event().getValue("quote").getAsString(),
                            ee.event().getValue("quoteAuthor").getAsString()
                    );
                    try {
                        qd.editQuote(nq.getId(), nq);
                        LOGGER.info("{} has edited quote {}.", ee.getUser(), nq.getId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ee.event().reply("Quote edited! Content is now:\n\"" + nq.getQuote() + "\" -- " + nq.getQuoteAuthor()).queue();
                    ee.removeListener();
                    event.removeListenerAndDestroy();
                })
                .build();
        il.addInteractable(interactable);
    }

    private EnumSet<Flag> calculateRequiredFlags(NamedQuote nq) {
        for (String s : Config.list().of("mchelper.quotes.derank-and-hide-from-motd-if-content-matches-none-of")) {
            if (containsIgnoreCase(nq.getQuote(), s) || containsIgnoreCase(nq.getQuoteAuthor(), s)) {
                return EnumSet.noneOf(Flag.class);
            }
        }
        return EnumSet.of(Flag.DERANK, Flag.EXCLUDE_QOTD);
    }
}