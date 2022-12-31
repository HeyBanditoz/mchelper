package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.tarkovmarket.Item;
import io.banditoz.mchelper.tarkovmarket.TarkovMarketSearcher;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class TarkovCommand extends Command {
    private TarkovMarketSearcher SEARCHER;

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
        if (SEARCHER == null) {
            // TODO should rework this as a constructor instead, need to rewrite part of CommandHandler for that.
            SEARCHER = new TarkovMarketSearcher(ce.getMCHelper());
        }
        List<MessageEmbed> results = SEARCHER.getMarketResultsBySearch(ce.getCommandArgsString()).stream().map(Item::getAsEmbed).toList();
        if (results.isEmpty()) {
            ce.sendReply("No matches found.");
            return Status.FAIL;
        }
        else {
            ce.sendEmbedPaginatedReplyWithPageNumber(results);
        }
        return Status.SUCCESS;
    }
}
