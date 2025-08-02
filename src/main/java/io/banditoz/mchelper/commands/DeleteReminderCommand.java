package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.database.Reminder;
import io.banditoz.mchelper.database.dao.RemindersDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresDatabase
public class DeleteReminderCommand extends Command {
    private final RemindersDao dao;

    @Inject
    public DeleteReminderCommand(RemindersDao dao) {
        this.dao = dao;
    }

    @Override
    public String commandName() {
        return "delremind";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Deletes a reminder.")
                .withParameters("<id>");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        int id = Integer.parseInt(ce.getCommandArgsString());
        Reminder r = dao.getReminderById(id);
        if (r == null || r.getAuthorId() != ce.getEvent().getAuthor().getIdLong()) {
            ce.sendReply("Reminder does not exist or you did not write the reminder.");
            return Status.FAIL;
        }
        else {
            dao.markDeleted(id);
            ce.sendReply("Reminder " + id + " marked for deletion.");
        }
        return Status.SUCCESS;
    }
}
