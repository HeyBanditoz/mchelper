package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.dictionary.Definition;
import io.banditoz.mchelper.dictionary.DictionaryResult;
import io.banditoz.mchelper.dictionary.DictionarySearcher;

public class DictionaryCommand extends Command {
    @Override
    public String commandName() {
        return "define";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("[num] <word>")
                .withDescription("Finds the definition of a word using Owlbot's API.");
    }

    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        DictionarySearcher ds = new DictionarySearcher(ce.getMCHelper());
        DictionaryResult result = null;
        int toLookup = 0;
        if (ce.getCommandArgs()[1].matches("\\d+")) {
            toLookup = Integer.parseInt(ce.getCommandArgs()[1]) - 1; // zero indexed
            result = ds.search(ce.getCommandArgs()[2]);
        }
        else {
            result = ds.search(ce.getCommandArgsString());
        }
        Definition d = result.getDefinitions().get(toLookup);
        String reply = result.getWord() + ", " +
                ((d.getType() == null) ? "unkn" : d.getType()) +
                ": " +
                d.getDefinition() +
                ((d.getExample() == null) ? "" : " *\"" + d.getExample() + "\"*") +
                " (" +
                (toLookup + 1) +
                " of " +
                result.getDefinitions().size() +
                ")"; // now this is a work of beauty
        ce.sendReply(reply);
    }
}
