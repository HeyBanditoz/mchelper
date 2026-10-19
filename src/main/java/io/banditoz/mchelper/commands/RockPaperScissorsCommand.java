package io.banditoz.mchelper.commands;

import java.util.concurrent.ThreadLocalRandom;

import static io.banditoz.mchelper.commands.logic.params.ParamTypes.enumOf;

import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Singleton;

@Singleton
public class RockPaperScissorsCommand extends BoundCommand {
    public enum Choice {
        ROCK,
        PAPER,
        SCISSORS;

        /** @return The choice this one beats. */
        Choice beats() {
            return switch (this) {
                case ROCK -> SCISSORS;
                case PAPER -> ROCK;
                case SCISSORS -> PAPER;
            };
        }

        /** @return The name as it appears in the reply, such as "Rock". */
        String display() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    private static final Choice[] CHOICES = Choice.values();

    @Override
    public String commandName() {
        return "rps";
    }

    @Override
    protected String description() {
        return "Chooses a random, Rock, Paper, or Scissors";
    }

    @Override
    protected Handler declare() {
        return Params.of()
                .arg("choice", "Your choice: rock, paper, or scissors.", enumOf(Choice.class))
                .handle(this::play);
    }

    private Status play(ICommandEvent ce, Choice choice) {
        Choice bot = CHOICES[ThreadLocalRandom.current().nextInt(CHOICES.length)];
        String matchup = choice.display() + " Vs. " + bot.display() + ": ";

        if (choice == bot) {
            ce.sendReply(matchup + "**Tie!**");
        }
        else if (choice.beats() == bot) {
            ce.sendReply(matchup + "**You win!**");
        }
        else {
            ce.sendReply(matchup + "**Bot wins!**");
        }
        return Status.SUCCESS;
    }
}
