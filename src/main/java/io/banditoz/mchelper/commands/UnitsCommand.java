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
    public void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        StringBuilder args = new StringBuilder();
        try {
            for (int i = 1; i < commandArgs.length; i++) {
                args.append(commandArgs[i]).append(" ");
            }
            if (!args.toString().contains("to")) {
                throw new IllegalArgumentException("Your units command must contain \"to\" to properly split your command to convert! Offending command: " + args.toString());
            }
            String[] argsSplit = args.toString().split(" to ");

            Process p = new ProcessBuilder("units", "-t", argsSplit[0], argsSplit[1]).start();

            p.waitFor(); // hacky for right now, but this is dangerous! make sure your bash commands won't hang
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            reader.close();
            sendReply(e, output.toString());
        }
        catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
