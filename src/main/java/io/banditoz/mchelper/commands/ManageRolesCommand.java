package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;

import static io.banditoz.mchelper.utils.RoleReactionUtils.buildMessage;
import static io.banditoz.mchelper.utils.RoleReactionUtils.removeRoleAndUpdateMessage;

import com.vdurmont.emoji.EmojiManager;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.database.dao.RolesDao;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.ReactionRoleMessage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

@Singleton
@RequiresDatabase
public class ManageRolesCommand extends Command {
    private final RolesDao dao;

    @Inject
    public ManageRolesCommand(RolesDao dao) {
        this.dao = dao;
    }

    @Override
    public String commandName() {
        return "manageroles";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParser(getDefaultArgs());
    }

    @Override
    public EnumSet<Permission> getRequiredPermissions() {
        return EnumSet.of(Permission.ADMINISTRATOR);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        String[] rawArgs = ce.getRawCommandArgs();
        String[] slicedArgs = Arrays.copyOfRange(rawArgs, 1, rawArgs.length);
        Namespace args = getDefaultArgs().parseArgs(slicedArgs);

        char thisPrefix = ce.getConfig().get(Config.PREFIX).charAt(0);
        if (args.get("init") != null && args.getBoolean("init")) {
            if (dao.containsGuild(ce.getGuild())) {
                ce.sendReply("This guild already has a message setup!");
                return Status.FAIL;
            }
            else if (args.getList("params") != null && args.getList("params").isEmpty()) {
                ce.sendReply("Please specify channel id!");
                return Status.FAIL;
            }
            else {
                TextChannel channel = ce.getGuild().getTextChannelById(args.getList("params").get(0).toString());
                MessageEmbed me = new EmbedBuilder()
                        .setDescription("""
                                React to be added to a role, remove your reaction to lose the role.

                                **Use `%cmanageroles -a <emote> <name> <role_id>` to add.**""".formatted(thisPrefix))
                        .setColor(Color.CYAN)
                        .build();
                channel.sendMessageEmbeds(me).queue(message -> {
                    try {
                        dao.init(channel, message, ce.getGuild());
                        ce.sendReply("Guild initialized!");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, throwable -> {
                    ce.sendExceptionMessage((Exception) throwable); // lol
                });
            }
        }
        else if (!dao.containsGuild(ce.getGuild())) {
            ce.sendReply("This guild is not registered, use `" + thisPrefix + " manageroles -i <channel_id>`");
            return Status.FAIL;
        }
        else if (args.get("deactivate") != null && args.getBoolean("deactivate")) {
            ReactionRoleMessage r = dao.getMessageRole(ce.getGuild());
            ce.getGuild().getTextChannelById(r.channelId()).retrieveMessageById(r.messageId()).queue(s -> s.delete().queue());
            dao.deactivate(ce.getGuild());
            ce.sendReply("Guild deactivated!");
        }
        else if (args.get("add_role") != null && args.getBoolean("add_role")) {
            Emoji userAddedEmoji = Emoji.fromFormatted(args.getList("params").get(0).toString());
            if (userAddedEmoji instanceof UnicodeEmoji ue && !EmojiManager.isEmoji(ue.getFormatted())) {
                ce.sendReply("Emoji " + ue + " is not an emoji!");
                return Status.FAIL;
            }
            boolean guildContainsEmoji = emojiIsInGuild(userAddedEmoji, ce.getGuild());
            if (!guildContainsEmoji) {
                ce.sendReply("This guild does not contain emoji " + userAddedEmoji.getAsReactionCode() + ". Refusing to add.");
                return Status.FAIL;
            }
            if (dao.getRoleCount(ce.getGuild()) > 20) {
                ce.sendReply("Adding would go over the limit of 20 reactions. Refusing to add. (Someday I'll split it between multiple messages.)");
                return Status.FAIL;
            }
            ReactionRoleMessage r = dao.getMessageRole(ce.getGuild());
            dao.addRole(
                    userAddedEmoji,
                    args.getList("params").get(1).toString(),
                    ce.getGuild(),
                    ce.getGuild().getRoleById(args.getList("params").get(2).toString())
            );
            Message message = ce.getGuild().getTextChannelById(r.channelId()).retrieveMessageById(r.messageId()).complete();
            MessageEmbed me = new EmbedBuilder().setDescription(buildMessage(dao.getRoles(ce.getGuild()), message)).setColor(Color.CYAN).build();
            message.editMessageEmbeds(me).queue();
            ce.sendReply("Role " + args.getList("params").get(1).toString() + " has been added!");
        }
        else if (args.get("remove_role") != null && args.getBoolean("remove_role")) {
            if (dao.guildContainsName(ce.getGuild(), args.getList("params").get(0).toString())) {
                Emoji emoji = dao.removeRole(ce.getGuild(), args.getList("params").get(0).toString());
                removeRoleAndUpdateMessage(dao, emoji, ce.getGuild());
                ce.sendReply("Removed role " + args.getList("params").get(0).toString() + "!");
            } else {
                ce.sendReply("The role " + args.getList("params").get(0).toString() + " does not exist!");
                return Status.FAIL;
            }
        }
        else if (args.get("rebuild") != null && args.getBoolean("rebuild")) {
            ReactionRoleMessage r = dao.getMessageRole(ce.getGuild());
            Message message = ce.getGuild().getTextChannelById(r.channelId()).retrieveMessageById(r.messageId()).complete();
            MessageEmbed me = new EmbedBuilder().setDescription(buildMessage(dao.getRoles(ce.getGuild()), message)).setColor(Color.CYAN).build();
            message.editMessageEmbeds(me).queue();
            ce.sendReply("Reaction role message rebuilt.");
        }
        return Status.SUCCESS;
    }

    private boolean emojiIsInGuild(Emoji e, Guild g) {
        if (e instanceof UnicodeEmoji) {
            return true; // unicode emojis are global
        }
        for (RichCustomEmoji emoji : g.getEmojis()) {
            if (emoji.getFormatted().equals(e.getFormatted())) {
                return true;
            }
        }
        return false;
    }

    private ArgumentParser getDefaultArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("roles").addHelp(false).build();
        parser.addArgument("-i", "--init")
                .action(Arguments.storeTrue())
                .help("initialize role management");
        parser.addArgument("--deactivate")
                .action(Arguments.storeTrue())
                .help("remove role management");
        parser.addArgument("-a", "--add-role")
                .action(Arguments.storeTrue())
                .help("adds a role");
        parser.addArgument("-r", "-d", "--remove-role")
                .action(Arguments.storeTrue())
                .help("removes a role");
        parser.addArgument("-b", "--rebuild")
                .action(Arguments.storeTrue())
                .help("rebuilds reaction role message");
        parser.addArgument("params")
                .help("the rest of the parameters for previous arguments")
                .nargs("*");
        return parser;
    }
}
