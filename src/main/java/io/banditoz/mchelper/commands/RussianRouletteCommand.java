package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RussianRouletteCommand extends Command {
    @Override
    public String commandName() {
        return "roulette";
    }

    @Override
    public int getCooldown() {
        return 120; // two minutes
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Kick a random person from your voice channel!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        GuildVoiceState vs = ce.getEvent().getMember().getVoiceState();
        if (!ce.getEvent().getGuild().getSelfMember().getPermissions().contains(Permission.VOICE_MOVE_OTHERS)) {
            ce.sendReply("I do not have VOICE_MOVE_OTHERS!");
            return Status.FAIL;
        }
        if (vs == null) {
            ce.sendReply("You are not in a voice channel.");
            return Status.FAIL;
        }
        ce.sendReply(ce.getEvent().getMember().getEffectiveName() + " spins the cylinder...");
        ce.getMCHelper().getSES().schedule(() -> {
            try {
                Member toKick = vs.getChannel().getMembers().get(ThreadLocalRandom.current().nextInt(vs.getChannel().getMembers().size()));
                vs.getGuild().kickVoiceMember(toKick).queue(unused -> ce.sendReply("**BANG!**"));
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(ce.getEvent(), ex, LOGGER);
            }
        }, 3, TimeUnit.SECONDS);
        return Status.SUCCESS;
    }
}
