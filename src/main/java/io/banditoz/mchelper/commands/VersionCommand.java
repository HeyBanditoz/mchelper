package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.Version;
import io.banditoz.mchelper.utils.Help;

public class VersionCommand extends Command {
    @Override
    public String commandName() {
        return "!version";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Returns the bot's version.");
    }

    @Override
    protected void onCommand() {
        String reply = "MCHelper, a Discord bot.\n" +
                "https://gitlab.com/HeyBanditoz/mchelper/commit/" + Version.GIT_SHA + "\n" +
                "Git revision date: " + Version.GIT_DATE + "\n" +
                "Build date: " + Version.BUILD_DATE;
        sendReply(reply);
    }
}
