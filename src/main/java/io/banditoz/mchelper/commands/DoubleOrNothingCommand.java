package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static io.banditoz.mchelper.commands.BlackJackCommand.GAME_IDENT;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.games.BlackJackGame;
import io.banditoz.mchelper.games.DoubleOrNothingGame;
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
public class DoubleOrNothingCommand extends Command {
    private final AccountManager am;
    private final GameManager gm;
    private final ScheduledExecutorService ses;
    private final InteractionListener interactionListener;
    private final TipGenerator tipGenerator;
    private final TipModifierCache tipModifierCache;
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);

    @Inject
    public DoubleOrNothingCommand(AccountManager am,
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
        DoubleOrNothingGame game = new DoubleOrNothingGame(ante, u, gm, am, ses, tipModifierCache);
        game.tryAndRemoveAnte("double or nothing ante");
        game.startPlaying();

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
        ce.addToContext("ANTE", ante);
        return Status.SUCCESS;
    }

    private void play(WrappedButtonClickEvent event) {
        DoubleOrNothingGame game = (DoubleOrNothingGame) event.getCommandEvent().getFromContext(GAME_IDENT);
        BigDecimal ante = (BigDecimal) event.getCommandEvent().getFromContext("ANTE");

        CommandEvent ce = event.getCommandEvent();
        MessageEmbed embed = game.generate(ante, event.getUser());
        Button bet = Button.primary(UUID.randomUUID().toString(), "Bet");
        Button stop = Button.danger(UUID.randomUUID().toString(), "Stop");
        MessageEditData m = new MessageEditBuilder().setComponents(ActionRow.of(bet, stop)).setEmbeds(embed).build();
        event.destroyThenReplaceWith(new ButtonInteractable(
                Map.of(bet, game::bet, stop, game::stop),
                ce.getEvent().getAuthor()::equals, 0, event.getMessage(), ce), m);
    }
}
