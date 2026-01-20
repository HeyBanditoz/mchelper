package io.banditoz.mchelper.commands;

import javax.annotation.Nullable;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Param;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.User;

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
        User user = null;
        if (!ce.getMentionedUsers().isEmpty()) {
            user = ce.getMentionedUsers().getFirst();
        }
        return go(ce, user);
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce,
                                 @Param(desc = "The member to fetch the balance for.") @Nullable User user) throws Exception {
        return go(sce, user);
    }

    private Status go(ICommandEvent ce, User user) throws Exception {
        if (ce.isFromGuild() && user != null && ce.getGuild().getMemberById(user.getIdLong()) == null) {
            throw new IllegalArgumentException("User is not in this guild.");
        }

        if (user == null) {
            ce.sendReply("Your balance: $" + AccountManager.format(accountManager.queryBalance(ce.getUser().getIdLong(), true)));
            return Status.SUCCESS;
        }
        else if (ce.isFromGuild() && ce.getGuild().getMemberById(user.getIdLong()) != null) {
            ce.sendReply(user.getAsMention() + "'s balance: $" + AccountManager.format(accountManager.queryBalance(user.getIdLong(), false)));
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("Unable to fetch that user's balance.");
            return Status.FAIL;
        }
    }
}
