package io.banditoz.mchelper;


public class Bot {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("dumpcommands")) {
            CommandsToMarkdown.commandsToMarkdown();
            System.exit(0);
        }
        else {
            new MCHelperImpl();
        }
    }
}
