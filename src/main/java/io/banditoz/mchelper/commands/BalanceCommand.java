package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Member;

@Singleton
@RequiresDatabase
public class BalanceCommand extends Command {
    private final AccountManager accountManager;

    @Inject
    public BalanceCommand(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public String commandName() {
        return "bal";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Checks your balance.")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (ce.getMentionedMembers().size() > 0) {
            Member m = ce.getMentionedMembers().get(0);
            ce.sendReply(m.getAsMention() + "'s balance: $" + AccountManager.format(accountManager.queryBalance(m.getIdLong(), false)));
        }
        else {
            ce.sendReply("Your balance: $" + AccountManager.format(accountManager.queryBalance(ce.getEvent().getAuthor().getIdLong(), true)));
        }
        return Status.SUCCESS;
    }
}
