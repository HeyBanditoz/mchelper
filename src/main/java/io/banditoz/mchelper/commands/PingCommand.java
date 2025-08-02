package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;

@Singleton
public class PingCommand extends Command {
    @Override
    public String commandName() {
        return "ping";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Returns the current websocket and API ping.")
                .withParameters(null);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        ce.getEvent().getJDA().getRestPing().queue((p) -> ce.sendReply("Pong! Websocket: " + ce.getEvent().getJDA().getGatewayPing() + "ms, REST: " + p + " ms."));
        return Status.SUCCESS;
    }
}
