package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static io.banditoz.mchelper.utils.ListUtils.extractNumRandomly;

public class PickCommand extends Command {
    private final Random random = new SecureRandom();

    @Override
    public String commandName() {
        return "pick";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[num] <options...>")
                .withDescription("Picks num from a list of options. If num is not specified, it will only pick one. " +
                        "Separate your words with 'or' or a space.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        int howMany = 1;
        String args;
        if (ce.getCommandArgs()[1].matches("\\d+")) {
            howMany = Integer.parseInt(ce.getCommandArgs()[1]);
            args = ce.getCommandArgsString().replaceFirst("\\d+ ", "");
        }
        else {
            args = ce.getCommandArgsString();
        }
        ArrayList<String> options;
        if (ce.getCommandArgsString().contains(" or ")) {
            options = new ArrayList<>(Arrays.asList(args.split("\\s+or\\s+")));
        }
        else {
            options = new ArrayList<>(Arrays.asList(args.split("\\s+")));
        }
        ce.sendReply(extractNumRandomly(howMany, options, random));
        return Status.SUCCESS;
    }
}
