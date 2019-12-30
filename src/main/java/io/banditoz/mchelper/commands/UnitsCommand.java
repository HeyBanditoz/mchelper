package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UnitsCommand extends Command {
    @Override
    public String commandName() {
        return "units";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<conversion>")
                .withDescription("Launches a conversion between two units separated by 'to'.");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        try {
            if (!ce.getCommandArgsString().contains("to")) {
                throw new IllegalArgumentException("Your units command must contain \"to\" to properly split your command to convert! Offending command: " + ce.getCommandArgsString());
            }
            String[] argsSplit = ce.getCommandArgsString().split(" to ");

            Process p = new ProcessBuilder("units", "-t", argsSplit[0], argsSplit[1]).start();

            p.waitFor(); // hacky for right now, but this is dangerous! make sure your bash commands won't hang
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            reader.close();
            ce.sendReply(output.toString());
        } catch (IOException | InterruptedException ex) {
            ce.sendExceptionMessage(ex);
        }
    }
}
