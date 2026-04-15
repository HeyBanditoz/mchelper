package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.games.BlackJackGame;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Singleton
@RequiresDatabase
public class BlackJackCommand extends Command {
    private final AccountManager am;
    private final GameManager gm;
    private final ScheduledExecutorService ses;
    private final InteractionListener interactionListener;
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);

    @Inject
    public BlackJackCommand(AccountManager am,
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
        return "blackjack";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false)
                .withParameters("<ante (with range " + LOWER.toPlainString() + " <= x <= " + UPPER.toPlainString() + ")>")
                .withDescription("Play double or nothing to win some money!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        User u = ce.getEvent().getAuthor();
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1].replace(",", ""));
        BlackJackGame game = new BlackJackGame(u, ante, gm, am, ses);
        game.tryAndRemoveAnte("blackjack ante");
        game.startPlaying();
        // yeah, I know it's weird, startPlaying adds the game to the game list, and play deals the cards
        game.play();
        // resolve instant wins
        if (game.getPlayersSum() == 21) {
            try {
                game.payout(true);
                ce.sendEmbedReply(game.win(BlackJackGame.WinState.BLACKJACK));
                game.stopPlaying();
                return Status.SUCCESS;
            } catch (Exception ex) {
                LOGGER.error("Error while paying out!", ex);
            }
        }
        else if (game.getDealerHand().get(1).rank().getValue() == 1 && game.getDealerSum() == 21) {
            ce.sendEmbedReply(game.lose());
            game.stopPlaying();
            return Status.SUCCESS;
        }
        MessageEmbed embed = game.generate();
        Button hit = Button.primary(UUID.randomUUID().toString(), "Hit");
        Button stay = Button.primary(UUID.randomUUID().toString(), "Stay");
        Button doubleDown = Button.danger(UUID.randomUUID().toString(), "Double Down");
        MessageCreateData m = new MessageCreateBuilder().setEmbeds(embed).setComponents(ActionRow.of(hit, stay, doubleDown)).build();
        ce.getEvent().getChannel().sendMessage(m).queue(success -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(hit, game::hit, stay, game::stand, doubleDown, game::doubleDown),
                    ce.getEvent().getAuthor()::equals,
                    0, success, ce);
            interactionListener.addInteractable(i);
        });

        return Status.SUCCESS;
    }
}
