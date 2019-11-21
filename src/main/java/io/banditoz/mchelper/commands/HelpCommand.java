package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.Help;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HelpCommand extends Command {
    private List<Command> commands = new ArrayList<>();

    @Override
    public String commandName() {
        return "!help";
    }

    public HelpCommand() {
        commands.add(this); // let's add ourselves into the mix, eh?
        for (Object registeredListener : MCHelper.getJDA().getRegisteredListeners()) {
            if (registeredListener instanceof Command) { // don't add the regex listeners
                commands.add((Command) registeredListener);
            }
        }
        commands.sort(Comparator.comparing(Command::commandName)); // sort alphabetically by command name
        logger.info(commands.size() + " commands registered.");
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("This screen.");
    }

    @Override
    protected void onCommand() {
        StringBuilder sb = new StringBuilder("Current list of commands:\n");
        for (Command c : commands) {
            sb.append(c.getHelp()).append("\n"); // TODO split this into multiple messages in sendReply somehow, this could grow too large in the future!
        }
        sendReply(sb.toString());
    }
}
