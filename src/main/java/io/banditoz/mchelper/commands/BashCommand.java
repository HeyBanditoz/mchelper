package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.ProcessUtils;
import io.banditoz.mchelper.utils.paste.Paste;
import io.banditoz.mchelper.utils.paste.PasteggUploader;

public class BashCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "bash";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParameters("<bash>")
                .withDescription("Executes bash if it is installed on system.");
    }

    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        Process p = new ProcessBuilder("bash", "-c", ce.getCommandArgsString()).start();
        String output = ProcessUtils.runProcess(p);
        if (output.length() > 2000) {
            ce.sendReply(new PasteggUploader(ce.getMCHelper()).uploadToPastegg(new Paste(output, 24)));
            return;
        }
        ce.sendReply(output);
    }
}
