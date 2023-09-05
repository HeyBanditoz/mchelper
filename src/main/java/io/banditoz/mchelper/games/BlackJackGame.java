package io.banditoz.mchelper.games;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlackJackGame extends Game {
    private BigDecimal currentAmount;
    private static final Map<User, Deck> DECKS = new ConcurrentHashMap<>();
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();
    private int dealerSum;
    private int playersSum;

    public BlackJackGame(User player, BigDecimal initialBet, MCHelper mcHelper) {
        super(5, 200_000, mcHelper, player, initialBet);
        this.currentAmount = initialBet;
        if (DECKS.get(player) == null) {
            DECKS.put(player, new Deck(2));
        }
    }

    public void play() {
        hitPlayer();
        hitDealer();
        hitPlayer();
        hitDealer();
    }

    public int hitPlayer() {
        playerHand.add(DECKS.get(player).draw());
        int aces = 0;
        int sum = 0;
        for (Card c : playerHand) {
            if (c.rank().getValue() == 1) {
                aces++;
            }
            else {
                if (c.rank().getValue() > 10) {
                    sum += 10;
                }
                else {
                    sum += c.rank().getValue();
                }
            }
        }
        sum += aces * 11;
        for (int i = 0; i < aces; i++) {
            if (sum > 21) {
                sum -= 10;
            }
            else {
                break;
            }
        }
        playersSum = sum;
        return sum;
    }

    public int hitDealer() {
        dealerHand.add(DECKS.get(player).draw());
        int aces = 0, sum = 0;
        for (Card c : dealerHand) {
            if (c.rank().getValue() == 1) {
                aces++;
            }
            else {
                if (c.rank().getValue() > 10) {
                    sum += 10;
                }
                else {
                    sum += c.rank().getValue();
                }
            }
        }
        sum += aces * 11;
        for (int i = 0; i < aces; i++) {
            if (sum > 21) {
                sum -= 10;
            }
            else {
                break;
            }
        }
        dealerSum = sum;
        return sum;
    }

    /**
     * Method called when a user intends to play for more money.
     */
    public void hit(WrappedButtonClickEvent wrappedEvent) {
        int sum = hitPlayer();
        try {
            if (sum == 21) {
                while (getDealerSum() < 17) {
                    hitDealer();
                }
                if (getDealerSum() == 21) {
                    standOff();
                    wrappedEvent.destroyThenAddReplayer(win(WinState.STANDOFF));
                    return;
                }
                payout(true);
                wrappedEvent.destroyThenAddReplayer(win(WinState.BLACKJACK));
            }
            else if (sum < 21) {
                wrappedEvent.getEvent().editMessageEmbeds(generate()).queue();
            }
            else {
                while (getDealerSum() < 17) {
                    hitDealer();
                }
                wrappedEvent.destroyThenAddReplayer(lose());
            }
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        }
    }

    /**
     * Method called when a user intends to cash out their winnings.
     */
    public void stand(WrappedButtonClickEvent wrappedEvent) {
        while (getDealerSum() < 17) {
            hitDealer();
        }
        try {
            if (getDealerSum() > 21 || getDealerSum() < getPlayersSum()) {
                payout(false);
                wrappedEvent.destroyThenAddReplayer(win(WinState.NORMAL));
            }
            else if (getDealerSum() == getPlayersSum()) {
                standOff();
                wrappedEvent.destroyThenAddReplayer(win(WinState.STANDOFF));
            }
            else {
                wrappedEvent.destroyThenAddReplayer(lose());
            }
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        } finally {
            stopPlaying();
        }
    }

    public MessageEmbed generate() {
        StringBuilder playerString = new StringBuilder(), dealerString = new StringBuilder();
        for (Card c : getPlayerHand()) {
            playerString.append(c.rank().getStringValue());
            playerString.append(c.suit().getStringValue());
            playerString.append("\n");
        }
        dealerString.append("\uD83C\uDFB4\n");
        for (int i = 1; i < getDealerHand().size(); i++) {
            dealerString.append(getDealerHand().get(i).rank().getStringValue());
            dealerString.append(getDealerHand().get(i).suit().getStringValue());
            dealerString.append("\n");
        }
        return new EmbedBuilder()
                .setTitle("Blackjack!")
                .setColor(Color.GREEN)
                .setDescription("You have $" + AccountManager.format(currentAmount) + " up for bet!"
                        + "\n\n *Your hand: " + getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand:*\n" + dealerString)
                .setFooter(player.getName() + " (" + getRemainingCards() + " cards left in deck.)", player.getEffectiveAvatarUrl())
                .build();
    }

    public MessageEmbed lose() {
        String playerString = getHandsAsString(getPlayerHand());
        String dealerString = getHandsAsString(getDealerHand());
        stopPlaying();
        return new EmbedBuilder()
                .setTitle("Blackjack!")
                .setColor(Color.RED)
                .setDescription("You lost $" + AccountManager.format(currentAmount) + "!"
                        + "\n\n*Final Hands:*"
                        + "\n*Your hand: " + getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand: " + getDealerSum() + "*\n" + dealerString)
                .setImage("https://i.kym-cdn.com/photos/images/newsfeed/001/421/797/f5a.gif")
                .setFooter(player.getName() + " (" + getRemainingCards() + " cards left in deck.)", player.getEffectiveAvatarUrl())
                .build();
    }

    public MessageEmbed win(WinState state) {
        String playerString = getHandsAsString(getPlayerHand());
        String dealerString = getHandsAsString(getDealerHand());
        String image = "", won = "You won $" + AccountManager.format(currentAmount) + "! Good job!";
        Color c = Color.GREEN;
        switch (state) {
            case NORMAL -> image = "https://i.pinimg.com/originals/d9/c7/5b/d9c75bdc08ceb24ca15a462c3eaa4a7f.gif";
            case BLACKJACK -> image = "https://media1.giphy.com/media/l2SpK57WpLOP845Rm/giphy.gif";
            case STANDOFF -> {
                won = "You hit a stand off and got your $" + AccountManager.format(currentAmount) + " back!";
                c = Color.YELLOW;
                image = "https://media1.tenor.com/images/70d670f744495cc6c02b50efbc14a1ca/tenor.gif?itemid=15805326";
            }
        }
        stopPlaying();
        return new EmbedBuilder()
                .setTitle("Blackjack!")
                .setColor(c)
                .setDescription(won
                        + "\n\n*Final Hands:*"
                        + "\n*Your hand: " + getPlayersSum() + "*\n" + playerString
                        + "\n*Their hand: " + getDealerSum() + "*\n" + dealerString)
                .setImage(image)
                .setFooter(player.getName() + " (" + getRemainingCards() + " cards left in deck.)", player.getEffectiveAvatarUrl())
                .build();
    }

    public String getHandsAsString(List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb.append(c.rank().getStringValue());
            sb.append(c.suit().getStringValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }

    public int getPlayersSum() {
        return playersSum;
    }

    public int getDealerSum() {
        return dealerSum;
    }

    public int getRemainingCards() {
        return DECKS.get(player).getRemainingCards();
    }

    public void payout(boolean twentyOne) throws Exception {
        if (twentyOne) {
            currentAmount = currentAmount.divide(TWO, RoundingMode.UP).multiply(THREE).add(currentAmount);
        }
        else {
            currentAmount = currentAmount.multiply(TWO);
        }
        am.add(currentAmount, player.getIdLong(), twentyOne ? "blackjack 21 winnings" : "blackjack winnings");
    }

    public void standOff() throws Exception {
        am.add(currentAmount, player.getIdLong(), "blackjack standoff");
    }

    public enum WinState {
        /** The player had a higher score than the dealer, without exceeding 20. */
        NORMAL,
        /** The player had a score of 21, beating the dealer. */
        BLACKJACK,
        /** The player and dealer tied. */
        STANDOFF
    }
}
