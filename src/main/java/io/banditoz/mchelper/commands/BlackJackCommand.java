package io.banditoz.mchelper.commands;

import com.github.ygimenez.method.Pages;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.games.BlackJackGame;
import io.banditoz.mchelper.games.Card;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BlackJackCommand extends Command {
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(500);
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
            MessageEmbed message = generate(ante, u,game);
            ce.getEvent().getChannel().sendMessage(message).queue(success -> {
                Pages.buttonize(success,
                        Map.of("\uD83C\uDCCF", (member, message1) -> hit(member.getUser(), message1),
                                "\uD83E\uDDCD", (member, message1) -> stand(member.getUser(), message1)),
                        false,
                        2147483647,
                        TimeUnit.DAYS,
                        ce.getEvent().getAuthor()::equals);
                if (game.getPlayersSum()==21) {
                    try {
                        game.payout(true);
                        success.editMessage(win(1,game.getCurrentBet(), u,game)).queue();
                        Pages.handler.removeEvent(success);
                        success.clearReactions().queue();
                        GAMES.remove(u);
                    } catch (Exception ex) {
                        LOGGER.error("Error while paying out!", ex);
                    }
                } else if (game.getDealerHand().get(1).getRANK().getValue()==1 && game.getDealerSum()==21) {
                    success.editMessage(lose(game.getCurrentBet(), u,game)).queue();
                    GAMES.remove(u);
                    Pages.handler.removeEvent(success);
                    success.clearReactions().queue();
                }
            });
        }
        return Status.SUCCESS;
    }

    /**
     * Method called when a user intends to play for more money.
     */
    private void hit(User user, Message message) {
        BlackJackGame game = GAMES.get(user);
        int sum = game.hitPlayer();
        try {
            if (sum == 21) {
                while (game.getDealerSum()<17) {
                    game.hitDealer();
                }
                if (game.getDealerSum()==21) {
                    game.standOff();
                    message.editMessage(win(2,game.getCurrentBet(), user,game)).queue();
                    Pages.handler.removeEvent(message);
                    message.clearReactions().queue();
                    GAMES.remove(user);
                    return;
                }
                game.payout(true);
                message.editMessage(win(1,game.getCurrentBet(), user,game)).queue();
                Pages.handler.removeEvent(message);
                message.clearReactions().queue();
                GAMES.remove(user);
            } else if (sum < 21) {
                message.editMessage(generate(game.getCurrentBet(), user,game)).queue();
                message.removeReaction("\uD83C\uDCCF",user).queue();
            }
            else {
                while (game.getDealerSum()<17) {
                    game.hitDealer();
                }
                message.editMessage(lose(game.getCurrentBet(), user,game)).queue();
                GAMES.remove(user);
                Pages.handler.removeEvent(message);
                message.clearReactions().queue();
            }
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        }
    }

    /**
     * Method called when a user intends to cash out their winnings.
     */
    private void stand(User user, Message message) {
        BlackJackGame game = GAMES.remove(user);
        while (game.getDealerSum()<17) {
            game.hitDealer();
        }
        try {
            if (game.getDealerSum()>21 || game.getDealerSum()<game.getPlayersSum()) {
                game.payout(false);
                message.editMessage(win(0,game.getCurrentBet(), user,game)).queue();
                Pages.handler.removeEvent(message);
                message.clearReactions().queue();
            } else if (game.getDealerSum()==game.getPlayersSum()) {
                game.standOff();
                message.editMessage(win(2,game.getCurrentBet(), user,game)).queue();
                Pages.handler.removeEvent(message);
                message.clearReactions().queue();
            } else{
                message.editMessage(lose(game.getCurrentBet(), user,game)).queue();
                Pages.handler.removeEvent(message);
                message.clearReactions().queue();
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
                .setTitle("BlackJack!")
                .setColor(Color.GREEN)
                .setDescription("You have $" + AccountManager.format(currentAmount) + " up for bet!"
                        + "\n\n *Your hand: " + game.getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand:*\n" + dealerString
                        + "\n*\uD83C\uDCCF to hit!\n\uD83E\uDDCD to stand.*")
                .setFooter(u.getName(), u.getEffectiveAvatarUrl())
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
                .setTitle("BlackJack!")
                .setColor(Color.RED)
                .setDescription("You lost $" + AccountManager.format(currentAmount) + "!"
                        + "\n\n*Final Hands:*"
                        + "\n*Your hand: " + game.getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand: " + game.getDealerSum() + "*\n" + dealerString)
                .setImage("https://i.kym-cdn.com/photos/images/newsfeed/001/421/797/f5a.gif")
                .setFooter(u.getName(), u.getEffectiveAvatarUrl())
                .build();
    }

    /**
     * @Param state 0 when normal win (dealer bust or beat dealer), 1 when blackjack, 2 when stand-off
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
                .setTitle("BlackJack!")
                .setColor(c)
                .setDescription(won
                        + "\n\n*Final Hands:*"
                        + "\n*Your hand: " + game.getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand: " + game.getDealerSum() + "*\n" + dealerString)
                .setImage(image)
                .setFooter(u.getName(), u.getEffectiveAvatarUrl())
                .build();
    }
}
