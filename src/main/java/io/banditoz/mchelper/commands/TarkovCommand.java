package io.banditoz.mchelper.commands;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.tarkovmarket.TarkovMarketResult;
import io.banditoz.mchelper.tarkovmarket.TarkovMarketSearcher;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TarkovCommand extends Command {
    @Override
    public String commandName() {
        return "tarkov";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false)
                .withDescription("Returns pricing information about a given item from the tarkov-market API.")
                .withParameters("<item name>");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        TarkovMarketSearcher searcher = new TarkovMarketSearcher(ce.getMCHelper());
        List<TarkovMarketResult> results = searcher.getMarketResultsBySearch(ce.getCommandArgsString());
        if (results.isEmpty()) {
            ce.sendReply("No matches found.");
            return Status.FAIL;
        }
        else if (results.size() == 1) {
            ce.sendEmbedReply(results.get(0).getAsEmbed());
        }
        else {
            List<Page> pages = new ArrayList<>(results.size());
            for (int i = 0; i < results.size(); i++) {
                MessageEmbed oldEmbed = results.get(i).getAsEmbed();
                MessageEmbed newEmbed = new EmbedBuilder(oldEmbed).setFooter("(" + (i + 1) + " of " + results.size() + ")").build();
                pages.add(new Page(PageType.EMBED, newEmbed));
            }
            ce.getEvent().getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                Pages.paginate(success, pages, 5, TimeUnit.MINUTES, ce.getEvent().getAuthor()::equals);
            });
        }
        return Status.SUCCESS;
    }
}
