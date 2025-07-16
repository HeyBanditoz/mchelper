package io.banditoz.mchelper.mtg;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static net.dv8tion.jda.api.utils.MarkdownSanitizer.sanitize;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.http.ScryfallClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class ScryfallService {
    private final ScryfallClient scryfallClient;
    private final Map<String, Emoji> emojis;
    private static final Emoji UNKNOWN = Emoji.fromUnicode("❓");
    private static final Pattern MANA_MATCHER = Pattern.compile("\\{(\\w+)}");
    private static final Color MTG_COLOR = new Color(201, 56, 20);

    public ScryfallService(MCHelper mcHelper) {
        this.scryfallClient = mcHelper.getHttp().getScryfallClient();
        emojis = mcHelper.getJDA().retrieveApplicationEmojis()
                .complete()
                .stream()
                .filter(emoji -> emoji.getName().startsWith("mana"))
                .collect(Collectors.toMap(ApplicationEmoji::getName, identity()));
    }

    public List<MessageEmbed> getMtgEmbedByFuzzy(String fuzzy) {
        ScryfallCard card = scryfallClient.getCardByFuzzySearch(fuzzy);
        List<MessageEmbed> list = new ArrayList<>(card.faces().size());
        int x = 0;
        for (ScryfallCardFace cardFace : card.faces()) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("%s %s".formatted(cardFace.name(), parseManaizedString(cardFace.manaCost())).trim());
            eb.setUrl(card.scryfallUri() + '&' + x++);
            if (cardFace.imageUris() == null && card.imageUris() != null) {
                eb.setThumbnail(card.imageUris().png());
            }
            else if (cardFace.imageUris() != null) {
                eb.setThumbnail(cardFace.imageUris().png());
            }
            String description = "%s\n%s".formatted(cardFace.typeLine(), parseManaizedString(sanitize(cardFace.oracleText())));
            if (cardFace.power() != null) {
                // assuming toughness is not null if power is, questionable?
                description += "\n%s/%s".formatted(cardFace.power(), cardFace.toughness());
            }
            if (cardFace.flavorText() != null) {
                description += "\n\n*%s*".formatted(sanitize(cardFace.flavorText()));
            }
            eb.setDescription(description);
            eb.setFooter("%s %s, %s".formatted(card.set().toUpperCase(), card.collectorNumber(), card.releasedAt().getYear()));
            eb.setColor(MTG_COLOR);
            MessageEmbed apply = eb.build();
            list.add(apply);
        }
        return list;
    }

    /**
     * Parses a Scryfall-formatted string (example: <code>{4}{W}{W}</code> or <code>Kicker {5}</code>
     * (where <code>{5}</code> represents something with 5 mana cost)) into a String
     * with those mana representations replaced with Discord emojis where applicable.
     */
    private String parseManaizedString(String string) {
        Matcher m = MANA_MATCHER.matcher(string);
        return m.replaceAll(matchResult -> emojis.getOrDefault("mana" + matchResult.group(1).toLowerCase(), UNKNOWN).getFormatted());
    }
}
