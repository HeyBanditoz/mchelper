package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.RoleObject;
import io.banditoz.mchelper.utils.database.dao.RolesDao;
import io.banditoz.mchelper.utils.database.dao.RolesDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Requires(database = true)
public class ManageRolesCommand extends Command {
    @Override
    public String commandName() {
        return "manageroles";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParser(getDefaultArgs());
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (!ce.getEvent().getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            ce.sendReply("You are not an Administrator!");
            return Status.FAIL;
        }
        Namespace args = getDefaultArgs().parseArgs(ce.getCommandArgsWithoutName());
        RolesDao rd = new RolesDaoImpl(ce.getDatabase());
        if (args.get("init") != null && args.getBoolean("init")) {
            if (rd.containsGuild(ce.getGuild())) {
                ce.sendReply("This guild already has a message setup!");
                return Status.FAIL;
            } else if (args.getList("params") != null && args.getList("params").isEmpty()) {
                ce.sendReply("Please specify channel id!");
                return Status.FAIL;
            } else {
                TextChannel channel = ce.getGuild().getTextChannelById(args.getList("params").get(0).toString());
                MessageEmbed me = new EmbedBuilder().setDescription("React to be added to a role, remove your reaction to lose the role.\n\n**Use `!manageroles -a <emote> <name> <role_id>` to add.**").setColor(Color.CYAN).build();
                Message message = channel.sendMessageEmbeds(me).complete();
                rd.init(channel,message,ce.getGuild());
                ce.getMCHelper().getRRL().getMessages().put(channel.getId(),message.getId());
                ce.sendReply("Guild initialized!");
            }
        } else if (!rd.containsGuild(ce.getGuild())) {
            ce.sendReply("This guild is not registered, use `!manageroles -i <channel_id>`");
            return Status.FAIL;
        } else if (args.get("deactivate") != null && args.getBoolean("deactivate")) {
            Map.Entry<String,String> entry = rd.deactivate(ce.getGuild());
            ce.getGuild().getTextChannelById(entry.getKey()).retrieveMessageById(entry.getValue()).queue(s -> {s.delete().queue();});
            ce.getMCHelper().getRRL().getMessages().remove(entry.getKey());
            ce.sendReply("Guild deactivated!");
        } else if (args.get("add_role") != null && args.getBoolean("add_role")) {
            if (rd.addRole(args.getList("params").get(0).toString(),args.getList("params").get(1).toString(),ce.getGuild(),ce.getGuild().getRoleById(args.getList("params").get(2).toString()))) {
                ce.getMCHelper().getRRL().addEvent(args.getList("params").get(0).toString());
                Map.Entry<String, String> entry = rd.getChannelAndMessageId(ce.getGuild());
                Message message = ce.getGuild().getTextChannelById(entry.getKey()).retrieveMessageById(entry.getValue()).complete();
                MessageEmbed me = new EmbedBuilder().setDescription(buildMessage(rd.getRoles(ce.getGuild()), message, ce.getGuild().getEmotes())).setColor(Color.CYAN).build();
                message.editMessageEmbeds(me).queue();
                ce.sendReply("Role " + args.getList("params").get(1).toString() + " has been added!");
            } else {
                ce.sendReply("There is already a role with duplicate emote, name, or role_id");
                return Status.FAIL;
            }
        } else if (args.get("remove_role") != null && args.getBoolean("remove_role")) {
            if (rd.containsName(args.getList("params").get(0).toString(), ce.getGuild())) {
                String emoteName = rd.removeRole(args.getList("params").get(0).toString(), ce.getGuild());
                boolean contains = false;
                Map.Entry<String, String> entry = rd.getChannelAndMessageId(ce.getGuild());
                Message message = ce.getGuild().getTextChannelById(entry.getKey()).retrieveMessageById(entry.getValue()).complete();
                for (Emote emote : ce.getGuild().getEmotes()) {
                    if (emote.getName().equals(emoteName.replace(":", ""))) {
                        contains = true;
                        message.clearReactions(emote).queue();
                    }
                }
                if (!contains) {
                    message.clearReactions(emoteName).queue();
                }
                MessageEmbed me = new EmbedBuilder().setDescription(buildMessage(rd.getRoles(ce.getGuild()), message, ce.getGuild().getEmotes())).setColor(Color.CYAN).build();
                message.editMessageEmbeds(me).queue();
                ce.sendReply("Removed role " + args.getList("params").get(0).toString() + "!");
                ce.getMCHelper().getRRL().addEvent(emoteName);
            } else {
                ce.sendReply("The role " + args.getList("params").get(0).toString() + " does not exist!");
                return Status.FAIL;
            }
        }
        return Status.SUCCESS;
    }

    private String buildMessage(List<RoleObject> map, Message message, List<Emote> emotes) {
        StringBuilder sb = new StringBuilder("React to be added to a role, remove your reaction to loose the role.\n");
        for (RoleObject e : map) {
            sb.append("\n");
            boolean contains = false;
            for (Emote emote : emotes) {
                if (emote.getName().equals(e.getEmote().replace(":",""))) {
                    contains = true;
                    sb.append(emote.getAsMention());
                    message.addReaction(emote).queue();
                }
            }
            if (!contains) {
                sb.append(e.getEmote());
                message.addReaction(e.getEmote()).queue();
            }
            sb.append(" ");
            sb.append(e.getName());
        }
        return sb.toString();
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
        parser.addArgument("params")
                .help("the rest of the parameters for previous arguments")
                .nargs("*");
        return parser;
    }
}
