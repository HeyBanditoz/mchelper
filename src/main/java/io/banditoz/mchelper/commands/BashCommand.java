package io.banditoz.mchelper.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BashCommand extends ElevatedCommand {
    @Override
    public String commandName() {
        return "!bash";
    }

    @Override
    public void onCommand() {
        try {
            Process p = new ProcessBuilder("bash", "-c", commandArgsString).start();
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
            sendReply(output.toString());
        }
        catch (InterruptedException | IOException ex) {
            sendExceptionMessage(ex);
        }
    }
}
