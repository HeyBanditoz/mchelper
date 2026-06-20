package io.banditoz.mchelper.commands;

import java.util.concurrent.ThreadLocalRandom;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.slash.Slash;
import io.banditoz.mchelper.commands.logic.slash.SlashCommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;

@Singleton
public class CoinFlipCommand extends Command {
    @Override
    public String commandName() {
        return "flip";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Flips a coin.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        handle(ce);
        return Status.SUCCESS;
    }

    @Slash
    public Status onSlashCommand(SlashCommandEvent sce) {
        handle(sce);
        return Status.SUCCESS;
    }

    private void handle(ICommandEvent ce) {
        ce.sendReply(ThreadLocalRandom.current().nextBoolean() ? "Heads!" : "Tails!");
    }
}
