package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.investing.Finance;
import io.banditoz.mchelper.investing.model.CompanyProfile;
import io.banditoz.mchelper.investing.model.Quote;
import io.banditoz.mchelper.investing.model.RawCandlestick;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;

public class StockCommand extends Command {
    @Override
    public String commandName() {
        return "stock";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParser(getDefualtArgs());
    }

    @Override
    protected int getCooldown() {
        return 3;
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Finance finance = new Finance(ce.getMCHelper());
        Namespace args = getDefualtArgs().parseArgs(ce.getCommandArgsWithoutName());
        String ticker = args.get("ticker");
        Quote quote = finance.getQuote(ticker);
        CompanyProfile cp = finance.getCompanyProfile(ticker);
        if (args.getBoolean("graph")) {
            RawCandlestick candles = finance.getCandlestickForTicker(ticker, args.getBoolean("yearly"));
            ce.sendEmbedImageReply(Finance.generateStockMessageEmbed(quote, cp), Finance.generateStockGraph(candles, cp));
        }
        else {
            ce.sendEmbedReply(Finance.generateStockMessageEmbed(quote, cp));
        }
        return Status.SUCCESS;
    }

    private ArgumentParser getDefualtArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("stock").addHelp(false).build();
        parser.description("Grabs stock information from a ticker.");
        parser.addArgument("-g", "--graph")
                .type(Boolean.class)
                .help("generate a stock graph")
                .setDefault(false);
        parser.addArgument("-y", "--yearly")
                .type(Boolean.class)
                .help("generated graph will show yearly data")
                .setDefault(false);
        parser.addArgument("ticker")
                .help("ticker to fetch");
        return parser;
    }
}
