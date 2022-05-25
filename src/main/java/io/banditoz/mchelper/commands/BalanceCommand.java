package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.Member;

@Requires(database = true)
public class BalanceCommand extends Command {
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
            ce.sendReply(m.getAsMention() + "'s balance: $" + AccountManager.format(ce.getMCHelper().getAccountManager().queryBalance(m.getIdLong(), false)));
        }
        else {
            ce.sendReply("Your balance: $" + AccountManager.format(ce.getMCHelper().getAccountManager().queryBalance(ce.getEvent().getAuthor().getIdLong(), true)));
        }
        return Status.SUCCESS;
    }
}
