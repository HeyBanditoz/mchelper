package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.games.DoubleOrNothingGame;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Singleton
@RequiresDatabase
public class DoubleOrNothingCommand extends Command {
    private final AccountManager am;
    private final GameManager gm;
    private final ScheduledExecutorService ses;
    private final InteractionListener interactionListener;
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);

    @Inject
    public DoubleOrNothingCommand(AccountManager am,
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
        return "don";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<ante (with range " + LOWER.toPlainString() + " <= x <= " + UPPER.toPlainString() + ")>")
                .withDescription("Play double or nothing to win some money!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        User u = ce.getEvent().getAuthor();
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1].replace(",", ""));
        // shall we play a game?
        DoubleOrNothingGame game = new DoubleOrNothingGame(ante, u, gm, am, ses);
        game.tryAndRemoveAnte("double or nothing ante");
        game.startPlaying();
        MessageEmbed embed = game.generate(ante, u);
        Button bet = Button.primary(UUID.randomUUID().toString(), "Bet");
        Button stop = Button.danger(UUID.randomUUID().toString(), "Stop");
        MessageCreateData m = new MessageCreateBuilder().setActionRow(bet, stop).setEmbeds(embed).build();
        ce.getEvent().getChannel().sendMessage(m).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(bet, game::bet, stop, game::stop),
                    ce.getEvent().getAuthor()::equals, 0, message, ce);
            interactionListener.addInteractable(i);
        });
        return Status.SUCCESS;
    }

}
