package io.banditoz.mchelper.commands;

import java.util.List;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.tarkovmarket.Item;
import io.banditoz.mchelper.tarkovmarket.TarkovMarketSearcher;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Singleton
public class TarkovCommand extends Command {
    private final TarkovMarketSearcher tarkovMarketSearcher;

    @Inject
    public TarkovCommand(TarkovMarketSearcher tarkovMarketSearcher) {
        this.tarkovMarketSearcher = tarkovMarketSearcher;
    }

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
        List<MessageEmbed> results = tarkovMarketSearcher.getMarketResultsBySearch(ce.getCommandArgsString()).stream().map(Item::getAsEmbed).toList();
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
