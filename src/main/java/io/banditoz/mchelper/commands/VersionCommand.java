package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.Version;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.format.DateTimeFormatter;

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
        String gitDate = TimeFormat.DATE_TIME_LONG.format(DateTimeFormatter.ISO_DATE_TIME.parse(Version.GIT_DATE));
        String buildDate = TimeFormat.DATE_TIME_LONG.format(DateTimeFormatter.ISO_DATE_TIME.parse(Version.BUILD_DATE));
        User owner = ce.getMCHelper().getOwner();

        MessageEmbed me = new EmbedBuilder()
                .setAuthor("MCHelper, a Discord bot.", "https://gitlab.com/HeyBanditoz/mchelper", "https://gitlab.com/uploads/-/system/project/avatar/13974445/MCHelper.png?width=64")
                .setDescription("[GitLab Commit](https://gitlab.com/HeyBanditoz/mchelper/commit/" + Version.GIT_SHA + ")\n" +
                        "Git revision date: " + gitDate + "\n" +
                        "Build date: " + buildDate)
                .setFooter("Instance managed by " + owner.getAsTag(), owner.getAvatarUrl())
                .build();
        ce.sendEmbedReply(me);
        return Status.SUCCESS;
    }
}
