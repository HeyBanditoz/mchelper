package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.games.DoubleOrNothingGame;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Requires(database = true)
public class DoubleOrNothingCommand extends Command {
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);
    private static final Map<User, DoubleOrNothingGame> GAMES = new ConcurrentHashMap<>(); // TODO add a proper game handler instead of this?

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
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1]);
        if (!(ante.compareTo(LOWER) >= 0 && ante.compareTo(UPPER) <= 0)) {
            ce.sendReply("Your bet must be between " + LOWER + " and " + UPPER + "!");
            return Status.FAIL;
        }
        if (GAMES.containsKey(u)) {
            ce.sendReply("You're already playing a game.");
            return Status.FAIL;
        }
        else {
            // shall we play a game?
            DoubleOrNothingGame game = new DoubleOrNothingGame(ante, u, ce.getMCHelper().getAccountManager());
            GAMES.put(u, game);
            try {
                ce.getMCHelper().getAccountManager().remove(ante, u.getIdLong(), "double or nothing ante");
            } catch (MoneyException ex) {
                GAMES.remove(u);
                ce.sendExceptionMessage(ex);
                return Status.EXCEPTIONAL_FAILURE;
            }
            MessageEmbed embed = generate(ante, u);
            Button bet = Button.primary(UUID.randomUUID().toString(), "Bet");
            Button stop = Button.danger(UUID.randomUUID().toString(), "Stop");
            Message m = new MessageBuilder().setActionRows(ActionRow.of(bet, stop)).setEmbeds(embed).build();
            ce.getEvent().getChannel().sendMessage(m).queue(message -> {
                ButtonInteractable i = new ButtonInteractable(
                        Map.of(bet, this::bet, stop, this::stop),
                        ce.getEvent().getAuthor()::equals, 0, message);
                ce.getMCHelper().getButtonListener().addInteractable(i);
            });
        }
        return Status.SUCCESS;
    }

    /**
     * Method called when a user intends to play for more money.
     */
    private void bet(WrappedButtonClickEvent wrappedEvent) {
        ButtonInteractionEvent event = wrappedEvent.getEvent();
        User user = event.getUser();

        DoubleOrNothingGame game = GAMES.get(user);
        if (game.play()) {
            event.editMessageEmbeds(generate(game.getCurrentBet(), user)).queue();
        }
        else {
            //wrappedEvent.getEvent().getMessage().editMessageEmbeds(lose(game.getCurrentBet(), user)).queue();
            wrappedEvent.removeListenerAndDestroy(lose(game.getCurrentBet(), user));
            GAMES.remove(user);
        }
    }

    /**
     * Method called when a user intends to cash out their winnings.
     */
    private void stop(WrappedButtonClickEvent wrappedEvent) {
        ButtonInteractionEvent event = wrappedEvent.getEvent();
        User user = event.getUser();

        DoubleOrNothingGame game = GAMES.remove(event.getUser());
        // TODO better exception handling
        try {
            game.payout();
            wrappedEvent.removeListenerAndDestroy(cashout(game.getCurrentBet(), user));
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        }
    }

    private MessageEmbed generate(BigDecimal currentAmount, User u) {
        return new EmbedBuilder()
                .setTitle("Double or Nothing!")
                .setColor(Color.GREEN)
                .setDescription("You currently have $" + AccountManager.format(currentAmount) + "!")
                .setFooter(u.getName(), u.getEffectiveAvatarUrl())
                .build();
    }

    private MessageEmbed lose(BigDecimal currentAmount, User u) {
        return new EmbedBuilder()
                .setTitle("Double or Nothing!")
                .setColor(Color.RED)
                .setDescription("You lost $" + AccountManager.format(currentAmount) + "!")
                .setImage("https://i.kym-cdn.com/photos/images/newsfeed/001/421/797/f5a.gif")
                .setFooter(u.getName(), u.getEffectiveAvatarUrl())
                .build();
    }

    private MessageEmbed cashout(BigDecimal currentAmount, User u) {
        return new EmbedBuilder()
                .setTitle("Double or Nothing!")
                .setColor(Color.GREEN)
                .setDescription("You cashed in $" + AccountManager.format(currentAmount) + "! Good job!")
                .setImage("https://i.pinimg.com/originals/d9/c7/5b/d9c75bdc08ceb24ca15a462c3eaa4a7f.gif")
                .setFooter(u.getName(), u.getEffectiveAvatarUrl())
                .build();
    }

}
