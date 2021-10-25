package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class PlayCommand extends Command {
    @Override
    public String commandName() {
        return "play";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Play some games from Discord.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        if (ce.getCommandArgsString().isEmpty()) {
            ce.sendReply(Arrays.toString(Game.class.getEnumConstants()));
            return Status.SUCCESS;
        }
        Optional<VoiceChannel> channel;
        if (ce.getEvent().getMember().getVoiceState().getChannel() != null) {
            GuildVoiceState vs = ce.getEvent().getMember().getVoiceState();
            if (vs.getChannel().getGuild().equals(ce.getEvent().getGuild())) {
                channel = Optional.ofNullable(ce.getEvent().getMember().getVoiceState().getChannel());
            }
            else {
                channel = findFirstJoinableVoiceChannelInGuild(ce);
            }
        }
        else {
            channel = findFirstJoinableVoiceChannelInGuild(ce);
        }

        if (channel.isEmpty()) {
            ce.sendReply("No voice channel found for this guild.");
            return Status.FAIL;
        }
        channel.ifPresent(vc ->
                vc.createInvite().setTargetApplication(Game.valueOf(ce.getCommandArgsString().toUpperCase(Locale.ROOT)).game).queue(invite ->
                        // TODO use CommandEvent but allow us to deny mentions and URLs maybe with a builder? Will need to rewrite that class anyway
                        ce.getEvent().getMessage().reply("Here's an invite for " + vc.getAsMention() + "\n" + invite.getUrl()).queue()
                )
        );
        return Status.SUCCESS;
    }

    private Optional<VoiceChannel> findFirstJoinableVoiceChannelInGuild(CommandEvent ce) {
        return ce.getGuild().getChannels().stream()
                .filter(vc -> vc.getType().isAudio())
                .filter(vc -> ce.getEvent().getMember().hasPermission(vc, Permission.VOICE_CONNECT))
                .map(VoiceChannel.class::cast) // there may be N voice channels in the stream by this point, can optimize this away, but it's pretty simple as it is
                .findFirst();
    }

    private enum Game {
        YOUTUBE_TOGETHER("755600276941176913"),
        POKER("755827207812677713"),
        BETRAYALIO("773336526917861400"),
        FISHINGTONIO("814288819477020702"),
        CHESS("832012586023256104"),
        AWKWORD("879863881349087252"),
        SPELLCAST("852509694341283871"),
        DOODLECREW("878067389634314250"),
        WORDSNACK("879863976006127627"),
        LETTERTILE("879863686565621790");

        private final String game;

        Game(String game) {
            this.game = game;
        }
    }
}
