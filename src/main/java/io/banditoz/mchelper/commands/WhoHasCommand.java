package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class WhoHasCommand extends Command {
    @Override
    public String commandName() {
        return "whohas";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<role ID>")
                .withDescription("Returns who has a certain role in the guild.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Role r = ce.getGuild().getRoleById(ce.getCommandArgsString());
        List<Member> members = ce.getGuild()
                .getMembers()
                .stream()
                .filter(member -> member.getRoles().contains(r))
                .sorted(Comparator.comparing(Member::getEffectiveName))
                .collect(Collectors.toList());
        StringJoiner memberMentions = new StringJoiner(", ");
        members.forEach(member -> memberMentions.add(member.getAsMention()));
        MessageEmbed e = new EmbedBuilder()
                .setColor(r.getColor())
                .setDescription("Members that have role *" + r.getName() + ":*\n" + memberMentions)
                .build();
        ce.sendEmbedReply(e);
        return Status.SUCCESS;
    }
}
