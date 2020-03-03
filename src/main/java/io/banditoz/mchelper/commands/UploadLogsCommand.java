package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.utils.Help;

import java.io.File;

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
    protected void onCommand(CommandEvent ce) {
        File htmlLogFile = new File("log.html");
        if (htmlLogFile.exists()) {
            if (htmlLogFile.canRead()) {
                ce.sendFile("Here iis this session's log file.", htmlLogFile);
            }
            else {
                ce.sendReply("Cannot read the HTML log file.");
            }
        }
        else {
            ce.sendReply("The HTML log file does not exist.");
        }
    }
}
