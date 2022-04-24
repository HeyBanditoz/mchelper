package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;

import java.util.Arrays;
import java.util.List;

public class Bot {
    public static void main(String[] args) throws Exception {
        List<String> argsList = Arrays.asList(args);
        if (argsList.size() > 0 && argsList.contains("dumpcommands")) {
            CommandsToMarkdown.commandsToMarkdown();
            System.exit(0);
        }
        else if (args.length > 0 && argsList.contains("gensettings")) {
            Settings s = SettingsManager.getDefaultSettings();
            SettingsManager.outputSettings(s);
            System.exit(0);
        }
        else {
            new MCHelperImpl();
        }
    }
}
