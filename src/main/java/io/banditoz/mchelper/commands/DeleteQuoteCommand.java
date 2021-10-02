package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.Permission;

import java.util.EnumSet;

@Requires(database = true)
public class DeleteQuoteCommand extends Command {
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
        QuotesDao dao = new QuotesDaoImpl(ce.getMCHelper().getDatabase());
        if (dao.deleteQuote(idToDelete, ce.getGuild())) {
            ce.sendReply("Quote successfully deleted.");
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("Quote was not deleted.");
            return Status.FAIL;
        }
    }
}
