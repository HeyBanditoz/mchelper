package io.banditoz.mchelper.games;

import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.entities.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlackJackGame {
    private BigDecimal currentBet;
    private final AccountManager accs;
    private final User player;
    private static final BigDecimal TWO = new BigDecimal("2"), THREE = new BigDecimal("3");
    private static final Map<User, Deck> decks = new ConcurrentHashMap<>();
    private List<Card> playerHand = new ArrayList<>(), dealerHand = new ArrayList<>();
    private int dealerSum,playersSum;

    public BlackJackGame(BigDecimal initialBet, User player, AccountManager accs) {
        this.currentBet = initialBet;
        this.player = player;
        this.accs = accs;
        if (decks.get(player) == null) {
            decks.put(player, new Deck(2));
        }
    }

    public void play() {
        hitPlayer();
        hitDealer();
        hitPlayer();
        hitDealer();
    }

    public int hitPlayer() {
        playerHand.add(decks.get(player).draw());
        int aces=0, sum=0;
        for (Card c : playerHand) {
            if (c.getRANK().getValue() == 1) {
                aces++;
            }
            else {
                if (c.getRANK().getValue()>10) {
                    sum+=10;
                } else {
                    sum += c.getRANK().getValue();
                }
            }
        }
        sum += aces*11;
        for (int i = 0; i < aces; i++) {
            if (sum>21) {
                sum-=10;
            } else {
                break;
            }
        }
        playersSum = sum;
        return sum;
    }

    public int hitDealer() {
        dealerHand.add(decks.get(player).draw());
        int aces=0, sum=0;
        for (Card c : dealerHand) {
            if (c.getRANK().getValue() == 1) {
                aces++;
            }
            else {
                if (c.getRANK().getValue()>10) {
                    sum+=10;
                } else {
                    sum += c.getRANK().getValue();
                }
            }
        }
        sum += aces*11;
        for (int i = 0; i < aces; i++) {
            if (sum>21) {
                sum-=10;
            } else {
                break;
            }
        }
        dealerSum = sum;
        return sum;
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
        return decks.get(player).getRemainingCards();
    }

    public void payout(boolean twentyOne) throws Exception {
        if (twentyOne) {
            currentBet = currentBet.divide(TWO, RoundingMode.UP).multiply(THREE).add(currentBet);
        } else {
            currentBet = currentBet.multiply(TWO);
        }
        accs.add(currentBet, player.getIdLong(), twentyOne ? "blackjack 21 winnings" : "blackjack winnings");
    }

    public void standOff() throws Exception {
        accs.add(currentBet, player.getIdLong(), "blackjack standoff");
    }

    public BigDecimal getCurrentBet() {
        return currentBet;
    }
}
