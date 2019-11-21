package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;

import java.util.ArrayList;
import java.util.Arrays;

import static io.banditoz.mchelper.utils.ListUtils.extractNumRandomly;

public class PickCommand extends Command {
    @Override
    public String commandName() {
        return "!pick";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[num] <options...>")
                .withDescription("Picks num from a list of options. If num is not specified, it will only pick one. " +
                        "Separate your words with 'or' or a space.");
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
}
