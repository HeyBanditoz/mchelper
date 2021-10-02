package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.games.BlackJackGame;
import io.banditoz.mchelper.games.Card;
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
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Requires(database = true)
public class BlackJackCommand extends Command {
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);
    private static final Map<User, BlackJackGame> GAMES = new ConcurrentHashMap<>();

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
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1]);
        if (!(ante.compareTo(LOWER) >= 0 && ante.compareTo(UPPER) <= 0)) {
            ce.sendReply("Your bet must be between " + LOWER + " and " + UPPER + "!");
            return Status.FAIL;
        }
        if (GAMES.containsKey(u)) {
            ce.sendReply("You're already playing a game.");
            return Status.FAIL;
        } else {
            BlackJackGame game = new BlackJackGame(ante, u, ce.getMCHelper().getAccountManager());
            GAMES.put(u, game);
            game.play();
            try {
                ce.getMCHelper().getAccountManager().remove(ante, u.getIdLong(), "blackjack ante");
            } catch (MoneyException ex) {
                GAMES.remove(u);
                ce.sendExceptionMessage(ex);
                return Status.EXCEPTIONAL_FAILURE;
            }

            // resolve instant wins
            if (game.getPlayersSum() == 21) {
                try {
                    game.payout(true);
                    ce.sendEmbedReply(win(1, game.getCurrentBet(), u, game));
                    GAMES.remove(u);
                    return Status.SUCCESS;
                } catch (Exception ex) {
                    LOGGER.error("Error while paying out!", ex);
                }
            } else if (game.getDealerHand().get(1).getRANK().getValue() == 1 && game.getDealerSum() == 21) {
                ce.sendEmbedReply(lose(game.getCurrentBet(), u, game));
                GAMES.remove(u);
                return Status.SUCCESS;
            }

            MessageEmbed embed = generate(ante, u,game);
            Button hit = Button.primary(UUID.randomUUID().toString(), "Hit");
            Button stay = Button.primary(UUID.randomUUID().toString(), "Stay");
            Message m = new MessageBuilder().setEmbeds(embed).setActionRows(ActionRow.of(hit, stay)).build();
            ce.getEvent().getChannel().sendMessage(m).queue(success -> {
                ButtonInteractable i = new ButtonInteractable(
                        Map.of(hit, this::hit, stay, this::stand),
                        ce.getEvent().getAuthor()::equals,
                        0, success);
                ce.getMCHelper().getButtonListener().addInteractable(i);
            });
        }
        return Status.SUCCESS;
    }

    /**
     * Method called when a user intends to play for more money.
     */
    private void hit(WrappedButtonClickEvent wrappedEvent) {
        User user = wrappedEvent.getEvent().getUser();
        BlackJackGame game = GAMES.get(user);

        int sum = game.hitPlayer();
        try {
            if (sum == 21) {
                while (game.getDealerSum()<17) {
                    game.hitDealer();
                }
                if (game.getDealerSum()==21) {
                    game.standOff();
                    wrappedEvent.removeListenerAndDestroy(win(2, game.getCurrentBet(), user, game));
                    GAMES.remove(user);
                    return;
                }
                game.payout(true);
                wrappedEvent.removeListenerAndDestroy(win(1, game.getCurrentBet(), user, game));
                GAMES.remove(user);
            } else if (sum < 21) {
                wrappedEvent.getEvent().editMessageEmbeds(generate(game.getCurrentBet(), user, game)).queue();
            }
            else {
                while (game.getDealerSum()<17) {
                    game.hitDealer();
                }
                wrappedEvent.removeListenerAndDestroy(lose(game.getCurrentBet(), user, game));
                GAMES.remove(user);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        }
    }

    /**
     * Method called when a user intends to cash out their winnings.
     */
    private void stand(WrappedButtonClickEvent wrappedEvent) {
        User user = wrappedEvent.getEvent().getUser();
        BlackJackGame game = GAMES.remove(user);

        while (game.getDealerSum()<17) {
            game.hitDealer();
        }
        try {
            if (game.getDealerSum()>21 || game.getDealerSum()<game.getPlayersSum()) {
                game.payout(false);
                wrappedEvent.removeListenerAndDestroy(win(0, game.getCurrentBet(), user, game));
            } else if (game.getDealerSum()==game.getPlayersSum()) {
                game.standOff();
                wrappedEvent.removeListenerAndDestroy(win(2, game.getCurrentBet(), user, game));
            } else {
                wrappedEvent.removeListenerAndDestroy(lose(game.getCurrentBet(), user, game));
            }
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        }
    }

    private MessageEmbed generate(BigDecimal currentAmount, User u, BlackJackGame game) {
        StringBuilder playerString = new StringBuilder(), dealerString = new StringBuilder();
        for (Card c : game.getPlayerHand()) {
            playerString.append(c.getRANK().getStringValue());
            playerString.append(c.getSUIT().getStringValue());
            playerString.append("\n");
        }
        dealerString.append("\uD83C\uDFB4\n");
        for (int i = 1; i < game.getDealerHand().size(); i++) {
            dealerString.append(game.getDealerHand().get(i).getRANK().getStringValue());
            dealerString.append(game.getDealerHand().get(i).getSUIT().getStringValue());
            dealerString.append("\n");
        }
        return new EmbedBuilder()
                .setTitle("Blackjack!")
                .setColor(Color.GREEN)
                .setDescription("You have $" + AccountManager.format(currentAmount) + " up for bet!"
                        + "\n\n *Your hand: " + game.getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand:*\n" + dealerString)
                .setFooter(u.getName() + " (" + game.getRemainingCards() + " cards left in deck.)", u.getEffectiveAvatarUrl())
                .build();
    }

    private MessageEmbed lose(BigDecimal currentAmount, User u, BlackJackGame game) {
        StringBuilder playerString = new StringBuilder(), dealerString = new StringBuilder();
        for (Card c : game.getPlayerHand()) {
            playerString.append(c.getRANK().getStringValue());
            playerString.append(c.getSUIT().getStringValue());
            playerString.append("\n");
        }
        for (Card c : game.getDealerHand()) {
            dealerString.append(c.getRANK().getStringValue());
            dealerString.append(c.getSUIT().getStringValue());
            dealerString.append("\n");
        }
        return new EmbedBuilder()
                .setTitle("Blackjack!")
                .setColor(Color.RED)
                .setDescription("You lost $" + AccountManager.format(currentAmount) + "!"
                        + "\n\n*Final Hands:*"
                        + "\n*Your hand: " + game.getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand: " + game.getDealerSum() + "*\n" + dealerString)
                .setImage("https://i.kym-cdn.com/photos/images/newsfeed/001/421/797/f5a.gif")
                .setFooter(u.getName() + " (" + game.getRemainingCards() + " cards left in deck.)", u.getEffectiveAvatarUrl())
                .build();
    }

    /**
     * @param state 0 when normal win (dealer bust or beat dealer), 1 when blackjack, 2 when stand-off
     */
    private MessageEmbed win(int state, BigDecimal currentAmount, User u, BlackJackGame game) {
        StringBuilder playerString = new StringBuilder(), dealerString = new StringBuilder();
        for (Card c : game.getPlayerHand()) {
            playerString.append(c.getRANK().getStringValue());
            playerString.append(c.getSUIT().getStringValue());
            playerString.append("\n");
        }
        for (Card c : game.getDealerHand()) {
            dealerString.append(c.getRANK().getStringValue());
            dealerString.append(c.getSUIT().getStringValue());
            dealerString.append("\n");
        }
        String image = "", won = "You won $" + AccountManager.format(currentAmount) + "! Good job!";
        Color c = Color.GREEN;
        switch (state) {
            case 0:
                image = "https://i.pinimg.com/originals/d9/c7/5b/d9c75bdc08ceb24ca15a462c3eaa4a7f.gif";
                break;
            case 1:
                image = "https://media1.giphy.com/media/l2SpK57WpLOP845Rm/giphy.gif";
                break;
            case 2:
                won = "You hit a stand off and got your $" + AccountManager.format(currentAmount) + " back!";
                c = Color.YELLOW;
                image = "https://media1.tenor.com/images/70d670f744495cc6c02b50efbc14a1ca/tenor.gif?itemid=15805326";
                break;
        }
        return new EmbedBuilder()
                .setTitle("Blackjack!")
                .setColor(c)
                .setDescription(won
                        + "\n\n*Final Hands:*"
                        + "\n*Your hand: " + game.getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand: " + game.getDealerSum() + "*\n" + dealerString)
                .setImage(image)
                .setFooter(u.getName() + " (" + game.getRemainingCards() + " cards left in deck.)", u.getEffectiveAvatarUrl())
                .build();
    }
}
