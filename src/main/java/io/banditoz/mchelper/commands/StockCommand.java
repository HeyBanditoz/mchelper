package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.finance.Finance;
import io.banditoz.mchelper.utils.finance.GlobalQuote;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class StockCommand extends Command {
    @Override
    public String commandName() {
        return "stock";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Grabs stock information and a graph from the ticker.")
                .withParameters("<ticker> [true|false to generate graph]");
    }

    @Override
    protected int getCooldown() {
        return 25; // long, because we are limited to 5 calls/minute
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        Finance finance = new Finance(ce.getMCHelper());
        try {
            String ticker = ce.getCommandArgs()[1];
            String realName = finance.getTickerBestMatch(ticker).getName();
            GlobalQuote gq = finance.getGlobalQuote(ticker);
            MessageEmbed e = finance.generateMessagEmbedFromGlobalQuote(gq, realName);
            boolean generateGraph = false;
            if (ce.getCommandArgs().length > 2) {
                generateGraph = Boolean.parseBoolean(ce.getCommandArgs()[2]);
            }
            if (generateGraph) {
                ce.sendEmbedImageReply(e, finance.generateStockGraph(ticker, "5min", realName));
            }
            else {
                ce.sendEmbedReply(e);
            }
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
