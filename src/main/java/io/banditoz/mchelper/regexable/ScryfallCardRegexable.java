package io.banditoz.mchelper.regexable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.sanitize;

import feign.FeignException;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.mtg.ScryfallService;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Singleton
public class ScryfallCardRegexable extends Regexable {
    private final ScryfallService scryfallService;
    private static final Pattern PATTERN = Pattern.compile("\\[\\[([^]]+)]]");

    @Inject
    public ScryfallCardRegexable(ScryfallService scryfallService) {
        this.scryfallService = scryfallService;
    }

    @Override
    public Pattern regex() {
        return PATTERN;
    }

    @Override
    protected Status onRegexCommand(RegexCommandEvent re) {
        if (!re.getConfig().get(Config.LISTEN_FOR_SCRYFALL).equals("true")) {
            return Status.NOT_CONFIGURED;
        }
        re.sendTyping();
        // Regexable just gives up the first match, but we need to get all matches in the message, thus, rematching!
        String actualContent = re.getEvent().getMessage().getContentRaw();
        Matcher messageMatcher = PATTERN.matcher(actualContent);
        List<MessageEmbed> embeds = new ArrayList<>(10);
        boolean anySuccess = false;
        for (int i = 0; messageMatcher.find(); i++) {
            String search = messageMatcher.group(1);
            try {
                embeds.addAll(scryfallService.getMtgEmbedByFuzzy(search));
                anySuccess = true;
            } catch (FeignException ex) {
                LOGGER.warn("\"Couldn't fetch card details.\" name=\"{}\"", search, ex);
                MessageEmbed errorEmbed = new EmbedBuilder()
                        .setTitle("HTTP Error " + ex.status())
                        .setDescription("For MTG card name `%s`".formatted(sanitize(search)))
                        .build();
                embeds.add(errorEmbed);
            }
            if (i >= 10) {
                break;
            }
        }
        re.sendEmbedReply(embeds);
        return anySuccess ? Status.SUCCESS : Status.EXCEPTIONAL_FAILURE;
    }
}
