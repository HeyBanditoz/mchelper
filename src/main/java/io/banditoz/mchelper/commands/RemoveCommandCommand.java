package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

public class RemoveCommandCommand extends ElevatedCommand {
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
        if (ce.getMCHelper().getCommandHandler().removeCommandByName(ce.getCommandArgs()[1])) {
            ce.sendReply("Command successfully removed for this runtime.");
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("Command not found.");
            return Status.FAIL;
        }
    }
}
