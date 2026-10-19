package io.banditoz.mchelper.commands;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static io.banditoz.mchelper.utils.ListUtils.extractNumRandomly;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Singleton;

@Singleton
public class PickCommand extends BoundCommand {
    private final Random random = new SecureRandom();

    @Override
    public String commandName() {
        return "pick";
    }

    @Override
    protected String description() {
        return "Picks num from a list of options. If num is not specified, it will only pick one. "
                + "Separate your words with 'or' or a space.";
    }

    @Override
    protected Handler declare() {
        return Params.of().rest("options",
                "How many to pick, then the options. Separate options by 'or', or a space.", this::pick);
    }

    private Status pick(ICommandEvent ce, String options) {
        String remaining = options;
        int howMany = 1;
        String num = remaining.split("\\s+")[0];
        if (num.matches("\\d+")) {
            howMany = Integer.parseInt(num);
            remaining = remaining.replaceFirst("\\d+ ", "");
        }

        ArrayList<String> choices;
        if (remaining.contains(" or ")) {
            choices = new ArrayList<>(Arrays.asList(remaining.split("\\s+or\\s+")));
        }
        else {
            choices = new ArrayList<>(Arrays.asList(remaining.split("\\s+")));
        }
        ce.sendReply(extractNumRandomly(howMany, choices, random));
        return Status.SUCCESS;
    }
}
