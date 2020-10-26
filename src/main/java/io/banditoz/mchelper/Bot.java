package io.banditoz.mchelper;

import javax.security.auth.login.LoginException;

public class Bot {
    public static void main(String[] args) throws LoginException, InterruptedException {
        if (args.length > 0 && args[0].equalsIgnoreCase("dumpcommands")) {
            CommandsToMarkdown.commandsToMarkdown();
            System.exit(0);
        }
        else {
            new MCHelperImpl();
        }
    }
}
