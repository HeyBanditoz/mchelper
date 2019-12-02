package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.Help;

public class PingCommand extends Command {
    @Override
    public String commandName() {
        return "!ping";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Returns the current websocket and API ping.")
                .withParameters(null);
    }

    @Override
    protected void onCommand() {
        e.getJDA().getRestPing().queue( (p) -> sendReply("Pong! Websocket: " + e.getJDA().getGatewayPing() + "ms, REST: " + p + " ms."));
    }
}
