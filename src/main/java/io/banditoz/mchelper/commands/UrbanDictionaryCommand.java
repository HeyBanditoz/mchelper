package io.banditoz.mchelper.commands;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.urbandictionary.UDDefinition;
import io.banditoz.mchelper.urbandictionary.UDResult;
import io.banditoz.mchelper.urbandictionary.UDSearcher;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UrbanDictionaryCommand extends Command {
    @Override
    public String commandName() {
        return "ud";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<word>").withDescription("Gets a definition from Urban Dictionary's API.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        UDSearcher ud = new UDSearcher(ce.getMCHelper());
        List<Page> pages = createPagesFromDefinition(ud.search(ce.getCommandArgsString()));

        if (pages.isEmpty()) {
            ce.sendReply("No definition found.");
            return Status.FAIL;
        }

        ce.getEvent().getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
            Pages.paginate(success, pages, 1, TimeUnit.MINUTES, ce.getEvent().getAuthor()::equals);
        });

        return Status.SUCCESS;
    }

    private List<Page> createPagesFromDefinition(UDResult result) {
        ArrayList<Page> pages = new ArrayList<>();
        List<UDDefinition> results = result.getResults();
        for (int i = 0; i < results.size(); i++) {
            UDDefinition definition = results.get(i);
            String definitionString = formatUrbanDictionaryLinkToMarkdown(definition.getDefinition(), true);
            String definitionExample = formatUrbanDictionaryLinkToMarkdown(definition.getExample(), false);
            String definitionExampleTwo = null;
            if (definitionExample.length() > 1024) {
                String originalDef = definitionExample;
                definitionExample = definitionExample.substring(0, 1022);
                definitionExampleTwo = originalDef.substring(1022);
            }

            pages.add(new Page(PageType.EMBED, new EmbedBuilder()
                    .setTitle(definition.getWord(), definition.getPermalink())
                    .setAuthor(definition.getAuthor() + " ↑" + definition.getThumbsUp() + " ↓" + definition.getThumbsDown())
                    .setDescription(definitionString)
                    .addField("Example", '*' + definitionExample + '*', true)
                    .addField(definitionExampleTwo != null ? "(cont'd)" : null, (definitionExampleTwo != null ? '*' + definitionExampleTwo + '*' : null), true)
                    .setTimestamp(definition.getWrittenOn())
                    .setFooter(i + 1 + "/" + result.getResults().size())
                    .build()));
        }
        return pages;
    }

    private String formatUrbanDictionaryLinkToMarkdown(String s, boolean isDef) {
        // TODO rewrite this so it uses some parser thing, because this sucks :)
        String n = s.replaceAll("\\[(.*?)]", "[$1](https://www.urbandictionary.com/define.php?term={$1}");
        while (n.contains("{")) {
            String requiredString = n.substring(n.indexOf("{") + 1, n.indexOf("}")).replace(" ", "%20");
            n = n.replaceFirst("\\{(.*?)}", requiredString + ")");
        }
        if (n.length() > 2048 && isDef) {
            return s.replace("[", "")
                    .replace("]", "");
        }
        else if (n.length() > 1024 && !isDef) {
            return s.replace("[", "")
                    .replace("]", "");
        }
        else {
            return n;
        }
    }
}
