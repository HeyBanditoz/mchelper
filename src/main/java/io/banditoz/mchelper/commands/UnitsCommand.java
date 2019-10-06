package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class UnitsCommand extends Command {
    @Override
    public String commandName() {
        return "!units";
    }

    @Override
    public void onCommand() {
        try {
            if (!commandArgsString.contains("to")) {
                throw new IllegalArgumentException("Your units command must contain \"to\" to properly split your command to convert! Offending command: " + commandArgsString);
            }
            String[] argsSplit = commandArgsString.split(" to ");

            Process p = new ProcessBuilder("units", "-t", argsSplit[0], argsSplit[1]).start();

            p.waitFor(); // hacky for right now, but this is dangerous! make sure your bash commands won't hang
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            reader.close();
            sendReply(output.toString());
        }
        catch (Exception ex) {
            sendExceptionMessage(ex);
        }
    }
}
