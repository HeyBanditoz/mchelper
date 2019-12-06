package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BashCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "!bash";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParameters("<bash>")
                .withDescription("Executes bash if it is installed on system.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        try {
            Process p = new ProcessBuilder("bash", "-c", ce.getCommandArgsString()).start();
            p.waitFor(); // hacky for right now, but this is dangerous! make sure your bash commands won't hang
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder("```");

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            reader.close();
            output.append("```");

            if (output.toString().compareTo("``````") == 0) { // bash gave us empty output, clarify this
                output = new StringBuilder("<no output>");
            }
            ce.sendReply(output.toString());
        }
        catch (InterruptedException | IOException ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
