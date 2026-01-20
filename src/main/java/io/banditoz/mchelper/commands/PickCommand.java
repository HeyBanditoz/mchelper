package io.banditoz.mchelper.commands;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static io.banditoz.mchelper.utils.ListUtils.extractNumRandomly;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Param;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;

@Singleton
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
        handle(ce.getCommandArgsString(), ce);
        return Status.SUCCESS;
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce,
                                 @Param(desc = "Options to pick from. Separate by 'or', or a space.") String args,
                                 @Param(desc = "Number of options to pick from.") @Nullable Integer count) {
        if (count == null) {
            handle(args, sce);
        }
        else {
            ArrayList<String> options;
            if (args.contains(" or ")) {
                options = new ArrayList<>(Arrays.asList(args.split("\\s+or\\s+")));
            }
            else {
                options = new ArrayList<>(Arrays.asList(args.split("\\s+")));
            }
            sce.sendReply(extractNumRandomly(count, options, random));
        }
        return Status.SUCCESS;
    }

    private void handle(String args, ICommandEvent ce) {
        int howMany = 1;
        String num = args.split("\\s+")[0];
        if (num.matches("\\d+")) {
            howMany = Integer.parseInt(num);
            args = args.replaceFirst("\\d+ ", "");
        }
        ArrayList<String> options;
        if (args.contains(" or ")) {
            options = new ArrayList<>(Arrays.asList(args.split("\\s+or\\s+")));
        }
        else {
            options = new ArrayList<>(Arrays.asList(args.split("\\s+")));
        }
        ce.sendReply(extractNumRandomly(howMany, options, random));
    }
}
