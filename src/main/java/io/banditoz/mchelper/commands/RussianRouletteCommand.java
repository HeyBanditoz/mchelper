package io.banditoz.mchelper.commands;

import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.banditoz.mchelper.commands.logic.*;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

@Singleton
public class RussianRouletteCommand extends Command {
    private final ScheduledExecutorService scheduledExecutorService;
    private final Random random = new SecureRandom();

    @Inject
    public RussianRouletteCommand(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

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
        List<Member> members = vs.getChannel().getMembers();
        if (!check(ce, vs)) {
            return Status.FAIL; // bot does not have permissions or not in a voice channel
        }
        ce.sendReply(ce.getEvent().getMember().getEffectiveName() + " spins the cylinder...");
        scheduledExecutorService.schedule(() -> {
            try {
                if (check(ce, vs)) {
                    if (random.nextDouble() <= 0.2) {
                        // please help me
                        int rand0 = random.nextInt(members.size());
                        int rand1 = random.nextInt(members.size());
                        while (rand0 == rand1) {
                            rand1 = random.nextInt(members.size());
                        }
                        int finalRand = rand1;
                        vs.getGuild().kickVoiceMember(members.get(rand0))
                                .queue(unused -> vs.getGuild().kickVoiceMember(members.get(finalRand))
                                .queue(unused1 -> ce.sendReply("**BANG BANG!!** The gun malfunctioned, and shot twice!"
                            )));
                    }
                    else {
                        Member toKick = members.get(random.nextInt(members.size()));
                        vs.getGuild().kickVoiceMember(toKick)
                                .queue(unused -> ce.sendReply("**BANG!**"));
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
