package io.banditoz.mchelper.commands;

import java.util.EnumSet;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.dao.QuotesDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.Permission;

@Singleton
@RequiresDatabase
public class DeleteQuoteCommand extends Command {
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
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<quote ID>")
                .withDescription("Deletes a quote from the database. You must have MANAGE_SERVER permissions on the guild.");
    }

    @Override
    protected EnumSet<Permission> getRequiredPermissions() {
        return EnumSet.of(Permission.MANAGE_SERVER);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        int idToDelete = Integer.parseInt(ce.getCommandArgs()[1]);
        if (dao.deleteQuote(idToDelete, ce.getGuild(), ce.getUser().getIdLong())) {
            ce.sendReply("Quote successfully deleted.");
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("Quote was not deleted.");
            return Status.FAIL;
        }
    }
}
