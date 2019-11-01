package io.banditoz.mchelper.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PickCommand extends Command {
    @Override
    public String commandName() {
        return "!pick";
    }

    @Override
    protected void onCommand() {
        if (commandArgsString.contains(" or ")) {
            List<String> options = Arrays.asList(commandArgsString.split("\\s+or\\s+"));
            logger.debug("Options (matches or): " + options.toString());
            int pos = ThreadLocalRandom.current().nextInt(options.size());
            logger.debug("\"" + options.get(pos) + "\" won! (" + pos + ")");
            sendReply(options.get(pos));
        }
        else {
            List<String> options = Arrays.asList(commandArgsString.split("\\s+"));
            logger.debug("Options (matches whitespace): " + options.toString());
            int pos = ThreadLocalRandom.current().nextInt(options.size());
            logger.debug("\"" + options.get(pos) + "\" won! (" + pos + ")");
            sendReply(options.get(pos));
        }
    }
}
