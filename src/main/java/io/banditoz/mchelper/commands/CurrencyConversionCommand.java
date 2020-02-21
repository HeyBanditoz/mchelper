package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.finance.FinancialUtils;
import io.banditoz.mchelper.utils.finance.RealtimeCurrencyExchangeRate;

public class CurrencyConversionCommand extends Command {
    @Override
    public String commandName() {
        return "currency";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false)
                .withDescription("Converts currency.")
                .withParameters("[from] [to]");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        try {
            RealtimeCurrencyExchangeRate currency = FinancialUtils.getCurrencyExchangeRate(ce.getCommandArgs()[1], ce.getCommandArgs()[2]);
            String reply = "1 " + currency.getFromCurrencyName() + " is " + currency.getExchangeRate().toPlainString()
                    + " " + currency.getToCurrencyName() + "\n*Last updated " + currency.getPrettyDateTime() + "*";
            ce.sendReply(reply);
        } catch (Exception ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
