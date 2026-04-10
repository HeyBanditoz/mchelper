package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.games.BlackJackGame;
import io.banditoz.mchelper.games.Game;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.TipGenerator;
import io.banditoz.mchelper.money.TipModifierCache;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

@Singleton
@RequiresDatabase
public class BlackJackCommand extends Command {
    private final AccountManager am;
    private final GameManager gm;
    private final ScheduledExecutorService ses;
    private final InteractionListener interactionListener;
    private final TipGenerator tipGenerator;
    private final TipModifierCache tipModifierCache;
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);
    public static final String GAME_IDENT = "BLACKJACK_GAME";

    @Inject
    public BlackJackCommand(AccountManager am,
                            GameManager gm,
                            ScheduledExecutorService ses,
                            InteractionListener interactionListener,
                            TipGenerator tipGenerator,
                            TipModifierCache tipModifierCache) {
        this.am = am;
        this.gm = gm;
        this.ses = ses;
        this.interactionListener = interactionListener;
        this.tipGenerator = tipGenerator;
        this.tipModifierCache = tipModifierCache;
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
        BlackJackGame game = new BlackJackGame(u, ante, gm, am, ses, tipModifierCache);
        game.tryAndRemoveAnte("blackjack ante");
        game.startPlaying();
        // yeah, I know it's weird, startPlaying adds the game to the game list, and play deals the cards
        game.play();

        Map<Button, Consumer<WrappedButtonClickEvent>> tipButtons = tipGenerator.getTipButtons(ce.getUser().getIdLong());
        tipButtons.forEach((button, consumer) -> tipButtons.replace(button, consumer.andThen(this::play)));
        ArrayList<Button> buttons = new ArrayList<>(tipButtons.keySet());
        Collections.shuffle(buttons);
        List<ActionRow> actionRows = ActionRow.partitionOf(buttons);
        MessageCreateData m = new MessageCreateBuilder()
                .setContent("This week is employer appreciation week! Would you like to tip your winnings to your dealer?")
                .setComponents(actionRows)
                .build();
        ce.getEvent().getChannel().sendMessage(m).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(tipButtons, ce.getEvent().getAuthor()::equals, 300, message, ce);
            interactionListener.addInteractable(i);
        });
        ce.addToContext(GAME_IDENT, game);
        return Status.SUCCESS;
    }

    private void play(WrappedButtonClickEvent event) {
        BlackJackGame game = (BlackJackGame) event.getCommandEvent().getFromContext(GAME_IDENT);
        if (game == null) {
            MessageEmbed wtf = new EmbedBuilder()
                    .setTitle("How did you manage that?")
                    .setDescription("The game wasn't carried over the command event.")
                    .setColor(new Color(176, 28, 124))
                    .build();
            event.removeListenerAndDestroy(wtf);
            return;
        }
        // resolve instant wins
        if (game.getPlayersSum() == 21) {
            try {
                game.payout(true);
                event.removeListenerAndDestroy(game.win(BlackJackGame.WinState.BLACKJACK));
                game.stopPlaying();
                return;
            } catch (Exception ex) {
                LOGGER.error("Error while paying out!", ex);
            }
        }
        else if (game.getDealerHand().get(1).rank().getValue() == 1 && game.getDealerSum() == 21) {
            event.removeListenerAndDestroy(game.lose());
            game.stopPlaying();
            return;
        }
        MessageEmbed embed = game.generate();
        Button hit = Button.primary(UUID.randomUUID().toString(), "Hit");
        Button stay = Button.primary(UUID.randomUUID().toString(), "Stay");
        Button doubleDown = Button.danger(UUID.randomUUID().toString(), "Double Down");
        MessageEditData m = new MessageEditBuilder().setEmbeds(embed).setComponents(ActionRow.of(hit, stay, doubleDown)).build();
        event.destroyThenReplaceWith(new ButtonInteractable(
                Map.of(hit, game::hit, stay, game::stand, doubleDown, game::doubleDown),
                event.getCommandEvent().getEvent().getAuthor()::equals,
                0, event.getMessage(), event.getCommandEvent()), m);
    }
}
