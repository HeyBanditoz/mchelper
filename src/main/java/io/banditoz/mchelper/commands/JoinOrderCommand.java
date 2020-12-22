package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Cooldown;
import io.banditoz.mchelper.commands.logic.CooldownType;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

public class JoinOrderCommand extends Command {
    @Override
    public String commandName() {
        return "joinorder";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Sends this guild's join order.");
    }

    @Override
    public Cooldown getDefaultCooldown() {
        return new Cooldown(30, ChronoUnit.SECONDS, CooldownType.PER_USER);
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        List<Member> joinSortedMembers = new ArrayList<>(ce.getGuild().getMembers()); // JDA returns an immutable list, make a new ArrayList so we can sort it instead
        joinSortedMembers.sort(Comparator.comparing(Member::getTimeJoined));
        StringJoiner sj = new StringJoiner("\n");
        for (Member member : joinSortedMembers) {
            sj.add(MarkdownSanitizer.escape("[" + (member.getUser().isBot() ? "B" : "U") + "] " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator()));
        }
        ce.sendReply("```\n" + sj.toString() + "\n```");
        return Status.SUCCESS;
    }
}
