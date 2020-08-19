package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;

import java.util.concurrent.ThreadLocalRandom;

public class RockPaperScissorsCommand extends Command {
    @Override
    public String commandName() {
        return "rps";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<rock/paper/scissors>")
                .withDescription("Chooses a random, Rock, Paper, or Scissors");
    }

    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        int result = ThreadLocalRandom.current().nextInt(3) + 1;
        String args = ce.getCommandArgs()[1];
        if (args.equalsIgnoreCase("rock") || args.equalsIgnoreCase("paper") || args.equalsIgnoreCase("scissors")) {
            if (args.equalsIgnoreCase("rock") && result == 1) {
                ce.sendReply("Rock Vs. Rock: **Tie!**");
            }
            else if (args.equalsIgnoreCase("paper") && result == 2) {
                ce.sendReply("Paper Vs. Paper: **Tie!**");
            }
            else if (args.equalsIgnoreCase("scissors") && result == 3) {
                ce.sendReply("Scissors Vs. Scissors: **Tie!**");
            }
            else if (args.equalsIgnoreCase("rock") && result == 2) {
                ce.sendReply("Rock Vs. Paper: **Bot wins!**");
            }
            else if (args.equalsIgnoreCase("rock") && result == 3) {
                ce.sendReply("Rock Vs. Scissors: **You win!**");
            }
            else if (args.equalsIgnoreCase("paper") && result == 1) {
                ce.sendReply("Paper Vs. Rock: **You win!**");
            }
            else if (args.equalsIgnoreCase("paper") && result == 3) {
                ce.sendReply("Paper Vs. Scissors: **Bot wins!**");
            }
            else if (args.equalsIgnoreCase("scissors") && result == 1) {
                ce.sendReply("Scissors Vs. Rock: **Bot wins!**");
            }
            else if (args.equalsIgnoreCase("scissors") && result == 2) {
                ce.sendReply("Scissors Vs. Paper: **You win!**");
            }
            else {
                ce.sendReply("An error occurred.");
            }
        }
        else {
            ce.sendReply("Invalid parameter! Try rock, paper, or scissors.");
        }
    }
}

