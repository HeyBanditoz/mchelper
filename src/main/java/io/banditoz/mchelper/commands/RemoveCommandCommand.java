package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RemoveCommandCommand extends ElevatedCommand {
    @Inject
    CommandHandler commandHandler; // TODO kludge

    @Override
    public String commandName() {
        return "removecommand";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true)
                .withDescription("Removes a command from the command handler. Persists until the bot restarts.")
                .withParameters("<command>");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (commandHandler.removeCommandByName(ce.getCommandArgs()[1])) {
            ce.sendReply("Command successfully removed for this runtime.");
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("Command not found.");
            return Status.FAIL;
        }
    }
}
