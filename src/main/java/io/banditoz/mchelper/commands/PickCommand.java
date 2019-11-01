package io.banditoz.mchelper.commands;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

public class PickCommand extends Command {
    private SecureRandom random = new SecureRandom();

    @Override
    public String commandName() {
        return "!pick";
    }

    @Override
    protected void onCommand() {
        int howMany = 1;
        if (commandArgs[1].matches("\\d+")) {
            howMany = Integer.parseInt(commandArgs[1]);
            commandArgsString = commandArgsString.replaceFirst("\\d+ ", "");
        }
        if (commandArgsString.contains(" or ")) {
            ArrayList<String> options = new ArrayList<>(Arrays.asList(commandArgsString.split("\\s+or\\s+")));
            sendReply(extractNumRandomly(howMany, options));
        }
        else {
            ArrayList<String> options = new ArrayList<>(Arrays.asList(commandArgsString.split("\\s+")));
            sendReply(extractNumRandomly(howMany, options));
        }
    }
    
    private String extractNumRandomly(int num, ArrayList<String> l) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int pos = random.nextInt(l.size());
            results.append(l.get(pos));
            if (i < num - 1) {
                results.append(", ");
            }
            l.remove(pos);
        }
        return results.toString();
    }
}
