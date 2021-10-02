package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.dictionary.Definition;
import io.banditoz.mchelper.dictionary.DictionaryResult;
import io.banditoz.mchelper.dictionary.DictionarySearcher;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

@Requires(settingsMethod = "getOwlBotToken")
public class DictionaryCommand extends Command {
    @Override
    public String commandName() {
        return "define";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<word>")
                .withDescription("Finds the definition of a word using Owlbot's API.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        DictionarySearcher ds = new DictionarySearcher(ce.getMCHelper());
        DictionaryResult result = ds.search(ce.getCommandArgsString());

        List<MessageEmbed> embeds = createPagesFromDefinition(result);
        ce.sendEmbedPaginatedReply(embeds);
        return Status.SUCCESS;
    }

    private List<MessageEmbed> createPagesFromDefinition(DictionaryResult dr) {
        List<MessageEmbed> pages = new ArrayList<>();
        for (int i = 0; i < dr.getDefinitions().size(); i++) {
            Definition d = dr.getDefinitions().get(i);
            pages.add(new EmbedBuilder()
                    .setTitle(dr.getWord() + ", " + valueOrNull(d.getType()))
                    .setDescription(valueOrNull(d.getDefinition()))
                    .addField("Example", d.getExample() == null ? "<no example>" : d.getExample(), true)
                    .setFooter(i + 1 + "/" + dr.getDefinitions().size()).build());
        }
        return pages;
    }

    /**
     * Returns the {@link String} if it is not {@code null}, else literal "null" if it is.
     *
     * @param s The {@link String} to check.
     * @return The {@link String} or literal "null"
     */
    private String valueOrNull(String s) {
        // TODO move this to the deserializer in Definition maybe?
        return s == null ? "null" : s;
    }
}
