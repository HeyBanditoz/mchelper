package io.banditoz.mchelper.commands;

import java.util.Arrays;
import java.util.StringJoiner;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandPermissions;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

@Singleton
public class GuildConfigCommand extends Command {
    @Override
    public String commandName() {
        return "config";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<key> <value>")
                .withDescription("Configure the bot for this guild. No arguments to view the configuration.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (ce.getEvent().getMember().hasPermission(Permission.MANAGE_SERVER) || CommandPermissions.isBotOwner(ce.getUser())) {
            if (ce.getRawCommandArgs().length == 1) {
                StringJoiner sj = new StringJoiner("\n");
                ce.getConfig().getAllConfigs().forEach((config, value) -> {
                    value = (value == null ? "null" : value);
                    sj.add(config.name() + ": " + MarkdownSanitizer.escape(value));
                });
                ce.sendReply("Configs for this guild: ```\n" + sj.toString() + "\n```");
                return Status.SUCCESS;
            }
            else if (ce.getRawCommandArgs().length == 2) {
                Config c = Config.valueOf(ce.getRawCommandArgs()[1]);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle(c.toString())
                        .addField("Description", c.getDescription(), false)
                        .addField("Default", "**" + c.getDefaultValue() + "**", false)
                        .addField("Current", "**" + MarkdownSanitizer.sanitize(ce.getConfig().get(c)) + "**", false)
                        .build();
                ce.sendEmbedReply(embed);
                return Status.SUCCESS;
            }
            else {
                Config c = Config.valueOf(ce.getRawCommandArgs()[1]);
                if (c.isBotOwnerLocked() && !CommandPermissions.isBotOwner(ce.getUser())) {
                    ce.sendReply("Sorry, you do not have permissions to set this config. Please get a bot owner to set it for you.");
                    return Status.BOT_OWNER_CHECK_FAILED;
                }
                String valueSetTo = ce.getConfig().set(Config.valueOf(ce.getRawCommandArgs()[1]), String.join(" ", Arrays.copyOfRange(ce.getRawCommandArgs(), 2, ce.getRawCommandArgs().length)));
                ce.sendReply("Set to `" + MarkdownSanitizer.escape(valueSetTo) + "`.");
                return Status.SUCCESS;
            }
        }
        else {
            ce.sendReply("You need MANAGE_SERVER.");
            return Status.NO_PERMISSION;
        }
    }
}
