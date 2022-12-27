package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.PollType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Requires(database = true)
public class PollCommand extends Command {
    private static final Pattern QUOTES_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    @Override
    public String commandName() {
        return "poll";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Create single-choice and multiple-choice anonymous polls.")
                .withParameters("<\"poll title\"> <\"poll type (single or multiple)\"> <\"questsion 1\"> [\"question 2\"] ...");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        // thanks to https://stackoverflow.com/a/366532/19609819
        List<String> matchList = new ArrayList<>();
        Matcher m = QUOTES_PATTERN.matcher(ce.getCommandArgsString().replace('“', '"').replace('”', '"'));
        while (m.find()) {
            if (m.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(m.group(1));
            } else if (m.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(m.group(2));
            } else {
                // Add unquoted word
                matchList.add(m.group());
            }
        }

        String title = matchList.get(0);
        PollType type = PollType.valueOf(matchList.get(1).toUpperCase(Locale.ROOT));
        List<String> questions = matchList.subList(2, matchList.size()).stream().filter(s -> !s.isBlank()).distinct().toList();
        ce.getMCHelper().getPollService().createPollAndSendMessage(title, questions, type, ce.getEvent());
        return Status.SUCCESS;
    }
}
