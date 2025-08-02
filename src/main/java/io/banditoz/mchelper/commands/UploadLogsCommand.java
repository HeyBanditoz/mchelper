package io.banditoz.mchelper.commands;

import java.io.File;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;

@Singleton
public class UploadLogsCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "uploadlogs";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true)
                .withDescription("Uploads this bot session's logs to the current channel")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        File htmlLogFile = new File("log.html");
        if (htmlLogFile.exists()) {
            if (htmlLogFile.canRead()) {
                ce.sendFile("Here is this session's log file.", htmlLogFile);
                return Status.SUCCESS;
            }
            else {
                ce.sendReply("Cannot read the HTML log file.");
                return Status.FAIL;
            }
        }
        else {
            ce.sendReply("The HTML log file does not exist.");
            return Status.FAIL;
        }
    }
}
