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
import net.dv8tion.jda.api.entities.Role;
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
                    dadBotChance <percent>: the chance dad bot will respond to an `i'm` message.
                        current value: %s percent
                    betBotChance <percent>: the chance bet bot will respond to a `bet` messsage.
                        current value: %s percent
                    prefix <character>: the prefix character to invoke commands.
                        current value: `%c`
                    voiceRoleId <id>: the role the bot will assign to people in VCs.
                        current value: `%s`
                    """.formatted(gc.getDefaultChannel(), gc.getPostQotdToDefaultChannel(), gc.getDadBotChance() * 100, gc.getBetBotChance() * 100, gc.getPrefix(), gc.getVoiceRoleId()));
            return Status.SUCCESS;
        }
        else if (ce.getEvent().getMember().hasPermission(Permission.MANAGE_SERVER)) {
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
                case "dadBotChance" -> {
                    double percent = parsePercent(value);
                    gc.setDadBotChance(percent);
                    reply = "Dad bot chance set to " + percent * 100 + "%.";
                }
                case "betBotChance" -> {
                    double percent = parsePercent(value);
                    gc.setBetBotChance(percent);
                    reply = "Bet bot chance set to " + percent * 100 + "%.";
                }
                case "prefix" -> {
                    char prefix = value.charAt(0);
                    gc.setPrefix(prefix);
                    reply = "Guild prefix set to `" + prefix + "`";
                }
                case "voiceRoleId" -> {
                    long roleId = Long.parseLong(value.replaceAll("[^0-9]", ""));
                    Role r = ce.getEvent().getGuild().getRoleById(roleId);
                    if (r == null) {
                        ce.sendReply("There is no role by that ID!");
                        return Status.FAIL;
                    }
                    if (!ce.getEvent().getGuild().getMemberById(ce.getEvent().getJDA().getSelfUser().getIdLong()).hasPermission(Permission.MANAGE_ROLES)) {
                        ce.sendReply("I need MANAGE_ROLES to do this.");
                        return Status.FAIL;
                    }
                    gc.setVoiceRoleId(roleId);
                    reply = "The bot will assign this role to members in VCs: " + r.getAsMention();
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

    private double parsePercent(String s) {
        double percent = Double.parseDouble(s) / 100;
        if (percent < 0 || percent > 1) {
            throw new IllegalArgumentException("Must be within 0 and 100!");
        }
        return percent;
    }
}
