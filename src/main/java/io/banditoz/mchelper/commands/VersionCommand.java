package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.Version;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;

public class VersionCommand extends Command {
    @Override
    public String commandName() {
        return "version";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters(null)
                .withDescription("Returns the bot's version.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        String reply = "MCHelper, a Discord bot.\n" +
                "https://gitlab.com/HeyBanditoz/mchelper/commit/" + Version.GIT_SHA + "\n" +
                "Git revision date: " + Version.GIT_DATE + "\n" +
                "Build date: " + Version.BUILD_DATE;
        ce.sendReply(reply);
        return Status.SUCCESS;
    }
}
