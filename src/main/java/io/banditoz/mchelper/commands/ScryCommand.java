package io.banditoz.mchelper.commands;

import java.util.List;
import java.util.Optional;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.STRING;
import static net.dv8tion.jda.api.utils.MarkdownSanitizer.sanitize;

import feign.FeignException;
import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.database.ScryfallPrinting;
import io.banditoz.mchelper.mtg.DatabaseScryfallCardLookup;
import io.banditoz.mchelper.mtg.ScryfallCard;
import io.banditoz.mchelper.mtg.ScryfallService;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.StringUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * Looks up a single Magic: the Gathering card by name. The same lookup the <code>[[card name]]</code>
 * {@link io.banditoz.mchelper.regexable.ScryfallCardRegexable regexable} does, but asked for explicitly, so it works
 * without LISTEN_FOR_SCRYFALL being turned on for the guild.
 */
@Singleton
@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // the database lookup only exists when a database is configured
public class ScryCommand extends BoundCommand {
    private final ScryfallService scryfallService;
    private final Optional<DatabaseScryfallCardLookup> databaseLookup;

    @Inject
    public ScryCommand(ScryfallService scryfallService, Optional<DatabaseScryfallCardLookup> databaseLookup) {
        this.scryfallService = scryfallService;
        this.databaseLookup = databaseLookup;
    }

    @Override
    public String commandName() {
        return "scry";
    }

    @Override
    protected String description() {
        return "Looks up a Magic: the Gathering card by approximate name. In text, quote names with spaces.";
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .arg("card", "Card to look up. The name does not need to be exact.", STRING, this::suggestCards)
                .optional("print", "Specific printing, like SLD 1512. Leave empty for the usual one.", STRING,
                        this::suggestPrintings)
                .handle(this::scry);
    }

    private Status scry(ICommandEvent ce, String card, Optional<String> print) {
        // printings are only known from the bulk data, so without a database the print is ignored
        if (print.isPresent() && !print.get().isBlank() && databaseLookup.isPresent()) {
            return handlePrinting(card, print.get(), ce);
        }
        return handle(card, ce);
    }

    /**
     * Suggests card names out of the local bulk data. Without a database there is nothing to suggest from, and going
     * to Scryfall's API on every keystroke isn't worth it, so the option is simply left as free text.
     */
    private List<Choice> suggestCards(CommandAutoCompleteInteractionEvent event) {
        String typed = event.getFocusedOption().getValue().trim();
        if (databaseLookup.isEmpty() || typed.isEmpty()) {
            return List.of();
        }
        return databaseLookup.get().findNamesStartingWith(typed, OptionData.MAX_CHOICES).stream()
                .map(name -> new Choice(name, name))
                .toList();
    }

    /**
     * Lists printings of whichever card is in the card option so far, labelled like
     * <code>SLD #1512 (Secret Lair Drop) · 2024</code>, with the value being <code>sld 1512</code>.
     */
    private List<Choice> suggestPrintings(CommandAutoCompleteInteractionEvent event) {
        OptionMapping card = event.getOption("card");
        if (databaseLookup.isEmpty() || card == null || card.getAsString().isBlank()) {
            return List.of();
        }
        String typed = event.getFocusedOption().getValue().trim();
        Optional<ScryfallCard> found = databaseLookup.get().findByFuzzyName(card.getAsString());
        if (found.isEmpty()) {
            return List.of();
        }
        return databaseLookup.get().findPrintings(found.get().id(), typed, OptionData.MAX_CHOICES).stream()
                .map(printing -> new Choice(describe(printing), printing.setCode() + ' ' + printing.collectorNumber()))
                .toList();
    }

    private static String describe(ScryfallPrinting printing) {
        String label = "%s #%s (%s) · %d".formatted(printing.setCode().toUpperCase(), printing.collectorNumber(),
                printing.setName(), printing.releasedAt().getYear());
        // no set name in the bulk data comes close, but Discord rejects anything past 100 characters outright
        return StringUtils.truncate(label, 97, false);
    }

    /**
     * Looks up a specific printing of a card.
     *
     * @param print Set code, optionally followed by a collector number, e.g. <code>sld 1512</code> or <code>fra</code>.
     *              A set code alone picks the most regular printing in that set.
     */
    private Status handlePrinting(String search, String print, ICommandEvent ce) {
        Optional<ScryfallCard> found = databaseLookup.get().findByFuzzyName(search);
        if (found.isEmpty()) {
            // not in the bulk data, so neither are its printings, but Scryfall's API may still know the card
            return handle(search, ce);
        }
        String[] parts = print.trim().split("\\s+", 2);
        String collectorNumber = parts.length > 1 ? parts[1].replaceFirst("^#", "") : "";
        Optional<ScryfallCard> printing = databaseLookup.get()
                .findPrinting(found.get().id(), parts[0], collectorNumber);
        if (printing.isEmpty()) {
            ce.sendReply("**%s** has no printing matching `%s`."
                    .formatted(sanitize(found.get().name()), sanitize(print)));
            return Status.FAIL;
        }
        ce.sendEmbedsReply(scryfallService.getMtgEmbeds(printing.get()));
        return Status.SUCCESS;
    }

    private Status handle(String search, ICommandEvent ce) {
        if (search == null || search.isBlank()) {
            ce.sendReply("Give me a card name to look up.");
            return Status.FAIL;
        }
        List<MessageEmbed> embeds;
        try {
            embeds = scryfallService.getMtgEmbedByFuzzy(search);
        }
        catch (FeignException ex) {
            // only reachable when the database has no match and the lookup falls through to Scryfall's API
            LOGGER.warn("\"Couldn't fetch card details.\" name=\"{}\"", search, ex);
            ce.sendReply("Scryfall returned HTTP %d looking up `%s`.".formatted(ex.status(), sanitize(search)));
            return Status.FAIL;
        }
        if (embeds.isEmpty()) {
            ce.sendReply("No MTG card matches `%s` closely enough.".formatted(sanitize(search)));
            return Status.FAIL;
        }
        // a card has at most a handful of faces, so this stays well under Discord's ten embed limit
        ce.sendEmbedsReply(embeds);
        return Status.SUCCESS;
    }
}
