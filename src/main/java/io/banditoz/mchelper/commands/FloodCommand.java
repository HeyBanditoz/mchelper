package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
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
    protected void onCommand(CommandEvent ce) throws Exception {
        int howMany = Integer.parseInt(ce.getCommandArgs()[1]);
        if (howMany < 1) {
            ce.sendExceptionMessage(new IllegalArgumentException("You must send at least one message."));
        }
        String args = ce.getCommandArgsString().replaceFirst("\\d+ ", "");
        if (ce.isElevated()) {
            if (howMany > 50) {
                ce.sendReply("You can't send more than 50 messages as an elevated user.");
            }
            else {
                flood(howMany, args, ce);
            }
        }
        else {
            if (howMany > 5) {
                ce.sendReply("You can't send more than 5 messages as a non-elevated user.");
            }
            else {
                flood(howMany, args, ce);
            }
        }
    }

    private void flood(int howMany, String message, CommandEvent ce) {
        message = message.replace("USER<", "<@");
        for (int i = 0; i < howMany; i++) {
            ce.sendUnsanitizedReply(message);
        }
    }
}
