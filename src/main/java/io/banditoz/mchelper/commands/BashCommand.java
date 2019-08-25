package io.banditoz.mchelper.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class BashCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "!bash";
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        StringBuilder args = new StringBuilder();
        try {
            for (int i = 1; i < commandArgs.length; i++) {
                args.append(commandArgs[i]).append(" ");
            }

            Process p = new ProcessBuilder("bash", "-c", args.toString()).start();
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
            sendReply(e, output.toString());
        }
        catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
