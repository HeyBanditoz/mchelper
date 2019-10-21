package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.dictionary.Definition;
import io.banditoz.mchelper.utils.dictionary.DictionaryResult;
import io.banditoz.mchelper.utils.dictionary.DictionarySearcher;

import java.io.IOException;

public class DictionaryCommand extends Command {
    @Override
    public String commandName() {
        return "!define";
    }

    @Override
    protected void onCommand() {
        if (SettingsManager.getInstance().getSettings().getOwlBotToken() == null) {
            sendReply("Your OwlBot token is not configured. It is required to look up dictionary definitions. Head to https://owlbot.info/ to get one.");
            return;
        }
        DictionaryResult result = null;
        int toLookup = 0;
        try {
            if (commandArgs[1].matches("\\d+")) {
                toLookup = Integer.parseInt(commandArgs[1]) - 1; // zero indexed
                result = DictionarySearcher.search(commandArgs[2]);
            }
            else {
                result = DictionarySearcher.search(commandArgs[1]);
            }
        } catch (IOException ex) {
            sendExceptionMessage(ex);
            return;
        }
        Definition d = result.getDefinitions().get(toLookup);
        String reply = result.getWord() + ", " +
                d.getType() +
                ": " +
                d.getDefinition() +
                ((d.getExample() == null) ? "" : " *\"" + d.getExample() + "\"*") +
                " (" +
                (toLookup + 1) +
                " of " +
                result.getDefinitions().size() +
                ")"; // now this is a work of beauty
        sendReply(reply);
    }
}
