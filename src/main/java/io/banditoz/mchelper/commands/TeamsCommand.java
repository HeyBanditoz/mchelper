package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.internal.entities.VoiceChannelImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

public class TeamsCommand extends Command {
    @Override
    public String commandName() {
        return "teams";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("sends a message with your voice channel divided into teams");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        VoiceChannel vc = ce.getEvent().getMember().getVoiceState().getChannel();
        if (vc == null) {
            ce.sendReply("You are not in a voice channel.");
            return Status.FAIL;
        }
        else {
            // TODO Optimize this algorithm!
            List<Member> members = new ArrayList<>(vc.getMembers());
            if (members.size() < 3) {
                ce.sendReply("Not enough people to form teams.");
                return Status.FAIL;
            }
            StringBuilder reply = new StringBuilder("**TEAMS**\n```properties\nTEAM-1: ");
            Collections.shuffle(members, ThreadLocalRandom.current());
            boolean even = members.size() % 2 == 0;
            boolean random = ThreadLocalRandom.current().nextBoolean();
            StringJoiner sj = new StringJoiner(", ");
            int limit = (even ? members.size() / 2 : random ? members.size() / 2 : members.size() / 2 + 1);
            for (int i = 0; i < limit; i++) {
                sj.add(members.remove(i).getEffectiveName());
            }
            reply.append(sj.toString());
            sj = new StringJoiner(", ");
            reply.append("\nTEAM-2: ");
            for (Member member : members) {
                sj.add(member.getEffectiveName());
            }
            reply.append(sj.toString()).append("```");
            ce.sendReply(reply.toString());
            return Status.SUCCESS;
        }
    }
}
