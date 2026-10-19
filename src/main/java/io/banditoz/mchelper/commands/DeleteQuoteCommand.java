package io.banditoz.mchelper.commands;

import java.util.EnumSet;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.INTEGER;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.database.dao.QuotesDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.Permission;

@Singleton
@RequiresDatabase
public class DeleteQuoteCommand extends BoundCommand {
    private final QuotesDao dao;

    @Inject
    public DeleteQuoteCommand(QuotesDao dao) {
        this.dao = dao;
    }

    @Override
    public String commandName() {
        return "delquote";
    }

    @Override
    protected String description() {
        return "Deletes a quote from the database. You must have MANAGE_SERVER permissions on the guild.";
    }

    @Override
    public EnumSet<Permission> getRequiredPermissions() {
        return EnumSet.of(Permission.MANAGE_SERVER);
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .arg("quoteid", "Quote ID to delete.", INTEGER)
                .handle(this::deleteQuote);
    }

    private Status deleteQuote(ICommandEvent ce, int quoteId) throws Exception {
        if (dao.deleteQuote(quoteId, ce.getGuild(), ce.getUser().getIdLong())) {
            ce.sendReply("Quote successfully deleted.");
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("Quote was not deleted.");
            return Status.FAIL;
        }
    }
}
