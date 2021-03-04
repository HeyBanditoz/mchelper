package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.*;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RussianRouletteCommand extends Command {
    @Override
    public String commandName() {
        return "roulette";
    }

    @Override
    public Cooldown getDefaultCooldown() {
        return new Cooldown(1, ChronoUnit.MINUTES, CooldownType.PER_USER);
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Kick a random person from your voice channel!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        GuildVoiceState vs = ce.getEvent().getMember().getVoiceState();
        if (!check(ce, vs)) {
            return Status.FAIL; // bot does not have permissions or not in a voice channel
        }
        ce.sendReply(ce.getEvent().getMember().getEffectiveName() + " spins the cylinder...");
        ce.getMCHelper().getSES().schedule(() -> {
            try {
                if (check(ce, vs)) {
                    if (ThreadLocalRandom.current().nextDouble() <= 0.2) {
                        int rand0 = ThreadLocalRandom.current().nextInt(vs.getChannel().getMembers().size());
                        int rand1 = ThreadLocalRandom.current().nextInt(vs.getChannel().getMembers().size());
                        while (rand0 == rand1) {
                            rand1 = ThreadLocalRandom.current().nextInt(vs.getChannel().getMembers().size());
                        }
                        int finalRand = rand1;
                        vs.getGuild().kickVoiceMember(vs.getChannel().getMembers().get(rand0)).queue(unused ->
                                vs.getGuild().kickVoiceMember(vs.getChannel().getMembers().get(finalRand)).queue(unused1 ->
                                        ce.sendReply("**BANG BANG!!** The gun malfunctioned, and shot twice!")));
                    }
                    else {
                        Member toKick = vs.getChannel().getMembers().get(ThreadLocalRandom.current().nextInt(vs.getChannel().getMembers().size()));
                        vs.getGuild().kickVoiceMember(toKick).queue(unused -> ce.sendReply("**BANG!**"));
                    }
                }
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(ce.getEvent(), ex, LOGGER);
            }
        }, 3, TimeUnit.SECONDS);
        return Status.SUCCESS;
    }

    /**
     * Checks preconditions for this command.
     *
     * @return true if the bot has the permission or the executor is in a voice channel, false if not.
     */
    private boolean check(CommandEvent ce, GuildVoiceState vs) {
        if (!ce.getEvent().getGuild().getSelfMember().getPermissions().contains(Permission.VOICE_MOVE_OTHERS)) {
            ce.sendReply("I do not have VOICE_MOVE_OTHERS!");
            return false;
        }
        if (vs.getChannel() == null) {
            ce.sendReply("You are not in a voice channel.");
            return false;
        }
        return true;
    }
}
