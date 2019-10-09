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
        List<String> options = Arrays.asList(commandArgsString.split("\\s+"));
        sendReply(options.get(ThreadLocalRandom.current().nextInt(options.size())));
    }
}
