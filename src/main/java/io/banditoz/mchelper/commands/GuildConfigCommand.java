package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

@Requires(database = true)
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
        GuildConfigDao dao = new GuildConfigDaoImpl(ce.getDatabase());
        GuildConfig gc = dao.getConfig(ce.getGuild());
        if (ce.getRawCommandArgs().length <= 1) {
            ce.sendReply("""
                    To set a config, do `!config <key> <value>`
                    Example: `!config postQotdToDefaultChannel true`
                    Available keys:
                    
                    defaultChannel <id>: the channel to send the quote of the day and join/leaves to.
                        current value: <#%s>
                    postQotdToDefaultChannel <boolean>: whether or not to post the QOTD at 16:00 UTC.
                        current value: %s
                    prefix <character>: the prefix character to invoke commands.
                        current value: `%c`
                    """.formatted(gc.getDefaultChannel(), gc.getPostQotdToDefaultChannel(), gc.getPrefix()));
            return Status.SUCCESS;
        }
        else if (ce.getEvent().getMember().hasPermission(Permission.ADMINISTRATOR)) {
            String value = ce.getRawCommandArgs()[2];
            String reply;
            switch (ce.getRawCommandArgs()[1]) {
                case "defaultChannel" -> {
                    // only digits, in case of mention
                    long channelId = Long.parseLong(value.replaceAll("[^0-9]", ""));
                    TextChannel channel = ce.getEvent().getGuild().getTextChannelById(channelId);
                    if (channel == null) {
                        ce.sendReply("There is no channel in this guild by that ID!");
                        return Status.FAIL;
                    }
                    else if (!channel.canTalk()) {
                        ce.sendReply("This bot cannot talk in that channel. Check permissions.");
                        return Status.FAIL;
                    }
                    gc.setDefaultChannel(channelId);
                    reply = "Default channel set to <#" + channelId + ">";
                }
                case "postQotdToDefaultChannel" -> {
                    boolean qotd = Boolean.parseBoolean(value);
                    long channel = gc.getDefaultChannel();
                    gc.setPostQotdToDefaultChannel(qotd);
                    reply = "Quotes of the day *will" + (qotd ? "" : " not") + "* be posted to <#" + channel + ">";
                }
                case "prefix" -> {
                    char prefix = value.charAt(0);
                    gc.setPrefix(prefix);
                    reply = "Guild prefix set to `" + prefix + "`";
                }
                default -> reply = "No key by supplied name.";
            }
            // commit new guild config to database
            dao.saveConfig(gc);
            ce.sendReply(reply);
            return Status.SUCCESS;
        }
        else {
            ce.sendReply("You must be an administrator of this guild to edit its config.");
            return Status.NO_PERMISSION;
        }
    }
}
