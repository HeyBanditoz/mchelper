package io.banditoz.mchelper.commands;

import java.util.Optional;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.commands.logic.params.ParamTypes;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Member;

@Singleton
@RequiresDatabase
public class BalanceCommand extends BoundCommand {
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
    protected String description() {
        return "Checks your balance.";
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .optional("user", "The member to fetch the balance for.", ParamTypes.MEMBER)
                .handle(this::balance);
    }

    private Status balance(ICommandEvent ce, Optional<Member> maybeMember) throws Exception {
        if (maybeMember.isEmpty()) {
            ce.sendReply("Your balance: $" + AccountManager.format(accountManager.queryBalance(ce.getUser().getIdLong(), true)));
            return Status.SUCCESS;
        }
        // MEMBER only binds someone in this guild, so there is nothing left to check here.
        Member member = maybeMember.get();
        ce.sendReply(member.getAsMention() + "'s balance: $" + AccountManager.format(accountManager.queryBalance(member.getIdLong(), false)));
        return Status.SUCCESS;
    }
}
