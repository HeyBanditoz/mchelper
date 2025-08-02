package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.TimeFormat;

@Singleton
public class UserInfoCommand extends Command {
    @Override
    public String commandName() {
        return "userinfo";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Returns information about a user.").withParameters("[mentions]");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Member m;
        List<Member> mentionedMembers = ce.getMentionedMembers();
        List<Member> joinSortedMembers = new ArrayList<>(ce.getGuild().getMembers()); // JDA returns an immutable list, make a new ArrayList so we can sort it instead
        joinSortedMembers.sort(Comparator.comparing(Member::getTimeJoined));
        if (mentionedMembers.isEmpty()) {
            m = ce.getEvent().getMember();
        }
        else {
            m = mentionedMembers.get(0);
        }
        int ourIndex = joinSortedMembers.indexOf(m);
        StringJoiner membersJoined = new StringJoiner(" → ");

        if (ourIndex - 2 >= 0) {
            membersJoined.add(MarkdownSanitizer.escape(joinSortedMembers.get(ourIndex - 2).getEffectiveName()));
        }
        if (ourIndex - 1 >= 0) {
            membersJoined.add(MarkdownSanitizer.escape(joinSortedMembers.get(ourIndex - 1).getEffectiveName()));
        }
        membersJoined.add("**" + MarkdownSanitizer.escape(joinSortedMembers.get(ourIndex).getEffectiveName()) + "**");
        if (ourIndex + 1 < joinSortedMembers.size()) {
            membersJoined.add(MarkdownSanitizer.escape(joinSortedMembers.get(ourIndex + 1).getEffectiveName()));
        }
        if (ourIndex + 2 < joinSortedMembers.size()) {
            membersJoined.add(MarkdownSanitizer.escape(joinSortedMembers.get(ourIndex + 2).getEffectiveName()));
        }

        StringJoiner roles = new StringJoiner(", ");
        for (Role role : m.getRoles()) {
            roles.add(role.getName());
        }
        MessageEmbed me = new EmbedBuilder()
                .setTitle(m.getUser().getEffectiveName())
                .setThumbnail(m.getUser().getEffectiveAvatarUrl())
                .setColor(m.getUser().isBot() ? Color.CYAN : Color.GREEN)
                .addField("ID", m.getUser().getId(), false)
                .addField("Nickname", m.getNickname() == null ? "<no nickname>" : m.getNickname(), false)
                .addField("Creation Date", TimeFormat.DATE_TIME_LONG.format(m.getTimeCreated()), false)
                .addField("Join Date", TimeFormat.DATE_TIME_LONG.format(m.getTimeJoined()), false)
                .addField("Join Order (" + (ourIndex + 1) + " of " + joinSortedMembers.size() + ")", membersJoined.toString(), false)
                .addField("Roles", roles.toString().isEmpty() ? "<no roles>": roles.toString(), false)
                .build();
        ce.sendEmbedReply(me);
        return Status.SUCCESS;
    }
}
