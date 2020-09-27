package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

public class FloodCommand extends Command {
    @Override
    public String commandName() {
        return "flood";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[num] <message>")
                .withDescription("Floods this channel with a message.");
    }

    @Override
    protected int getCooldown() {
        return 10;
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        int howMany = Integer.parseInt(ce.getCommandArgs()[1]);
        if (howMany < 1) {
            ce.sendReply("You must send at least one message.");
            return Status.FAIL;
        }
        String args = ce.getCommandArgsString().replaceFirst("\\d+ ", "");
        if (ce.isElevated()) {
            if (howMany > 50) {
                ce.sendReply("You can't send more than 50 messages as an elevated user.");
                return Status.FAIL;
            }
            else {
                flood(howMany, args, ce);
            }
        }
        else {
            if (howMany > 5) {
                ce.sendReply("You can't send more than 5 messages as a non-elevated user.");
                return Status.FAIL;
            }
            else {
                flood(howMany, args, ce);
            }
        }
        return Status.SUCCESS;
    }

    private void flood(int howMany, String message, CommandEvent ce) {
        message = message.replace("USER<", "<@");
        for (int i = 0; i < howMany; i++) {
            ce.sendUnsanitizedReply(message);
        }
    }
}
