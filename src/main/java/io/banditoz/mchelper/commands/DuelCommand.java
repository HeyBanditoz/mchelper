package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.games.DuelGame;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Singleton
@RequiresDatabase
public class DuelCommand extends Command {
    private final AccountManager am;
    private final GameManager gm;
    private final ScheduledExecutorService ses;
    private final InteractionListener interactionListener;
    private final static Emoji CHECK = Emoji.fromUnicode("\u2611\uFE0F");
    private final static Emoji CANCEL = Emoji.fromUnicode("⛔");

    @Inject
    public DuelCommand(AccountManager am,
                       GameManager gm,
                       ScheduledExecutorService ses,
                       InteractionListener interactionListener) {
        this.am = am;
        this.gm = gm;
        this.ses = ses;
        this.interactionListener = interactionListener;
    }

    @Override
    public String commandName() {
        return "duel";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<ante>")
                .withDescription("Duel with your channel for money!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        // don't run from DM, TODO do annotation to guard for this instead?
        if (ce.getChannelType() == ChannelType.PRIVATE) {
            ce.sendReply("You cannot run this from a DM!");
            return Status.FAIL;
        }
        User u = ce.getEvent().getAuthor();
        BigDecimal ante = new BigDecimal(ce.getCommandArgsString().replace(",", ""));
        DuelGame game = new DuelGame(ante, u, gm, am, ses);
        game.tryAndRemoveAnte("duel ante");
        game.startPlaying();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Duel!")
                .setDescription("Enter a duel with " + u.getAsMention() + " for $" + AccountManager.format(ante) + "!")
                .setColor(Color.GREEN)
                .build();
        Button play = Button.primary(UUID.randomUUID().toString(), CHECK);
        Button cancel = Button.danger(UUID.randomUUID().toString(), CANCEL);
        MessageCreateData m = new MessageCreateBuilder().setComponents(ActionRow.of(play, cancel)).setEmbeds(embed).build();
        ce.getEvent().getChannel().sendMessage(m).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(play, game::enterGame, cancel, game::cancel),
                    user -> true, 0, message, ce);
            interactionListener.addInteractable(i);
        });

        return Status.SUCCESS;
    }
}
