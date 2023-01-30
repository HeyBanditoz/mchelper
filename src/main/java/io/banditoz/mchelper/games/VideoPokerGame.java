package io.banditoz.mchelper.games;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.games.poker.PokerResult;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.StringJoiner;

import static io.banditoz.mchelper.games.poker.PokerScoringEngine.evaluateVideoPoker;

public class VideoPokerGame extends Game {
    private final Deck deck = new Deck();
    private final CardHoldingPair[] hand = new CardHoldingPair[5];
    private PokerResult currentResult = PokerResult.LOSS;
    private static final Logger log = LoggerFactory.getLogger(VideoPokerGame.class);

    public VideoPokerGame(BigDecimal ante, User u, MCHelper mcHelper) {
        super(100, 500, mcHelper, u, ante);
    }

    @Override
    public void startPlaying() {
        super.startPlaying();
        for (int i = 0; i < 5; i++) {
            hand[i] = new CardHoldingPair(deck.draw(), true);
        }
        currentResult = evaluateVideoPoker(Arrays.stream(hand).map(CardHoldingPair::c).toList());
    }

    public void mark(WrappedButtonClickEvent e) {
        int i = Integer.parseInt(e.getEvent().getButton().getLabel()) - 1;
        markAtIndex(i);
        e.getEvent().editMessageEmbeds(generateEmbed()).queue();
    }

    public void submit(WrappedButtonClickEvent e) {
        for (int i = 0; i < hand.length; i++) {
            CardHoldingPair p = hand[i];
            if (p.m) {
                hand[i] = new CardHoldingPair(deck.draw(), true);
            }
        }
        currentResult = evaluateVideoPoker(Arrays.stream(hand).map(CardHoldingPair::c).toList());
        EmbedBuilder b = generateBuilder();
        b.setColor(currentResult == PokerResult.LOSS ? Color.RED : Color.GREEN);
        payout(e, b);
    }

    public void invert(WrappedButtonClickEvent e) {
        for (int i = 0; i < hand.length; i++) {
            CardHoldingPair p = hand[i];
            hand[i] = new CardHoldingPair(p.c, !p.m);
        }
        e.getEvent().editMessageEmbeds(generateEmbed()).queue();
    }

    public void holdAllAndSubmit(WrappedButtonClickEvent e) {
        for (int i = 0; i < hand.length; i++) {
            CardHoldingPair p = hand[i];
            hand[i] = new CardHoldingPair(p.c, false);
        }
        submit(e);
    }

    public boolean markAtIndex(int i) {
        CardHoldingPair p = hand[i];
        hand[i] = new CardHoldingPair(p.c, !p.m);
        return !p.m;
    }

    public MessageEmbed generateEmbed() {
        return generateBuilder().build();
    }

    private EmbedBuilder generateBuilder() {
        PokerResult currentHand = evaluateVideoPoker(Arrays.stream(hand).map(CardHoldingPair::c).toList());
        StringJoiner sj = new StringJoiner("\n");
        for (CardHoldingPair p : hand) {
            sj.add(p.toString());
        }
        return new EmbedBuilder()
                .setTitle("Video Poker!")
                .setDescription(sj.toString())
                .setFooter(currentHand.toString());
    }

    private void payout(WrappedButtonClickEvent e, EmbedBuilder me) {
        stopPlaying();
        BigDecimal winnings = ante.multiply(new BigDecimal(currentResult.getMultiplier()));
        StringJoiner sj = new StringJoiner("\n");
        for (CardHoldingPair p : hand) {
            sj.add(p.c.toString());
        }
        me.setDescription("You won $" + AccountManager.format(winnings) + "!\n" + sj);
        try {
            if (currentResult != PokerResult.LOSS) {
                am.add(winnings, player.getIdLong(), "video poker " + currentResult.toString());
            }
        } catch (Exception ex) {
            log.error("Error while playing out!", ex);
        } finally {
            e.removeListenerAndDestroy(me.build());
        }
    }

    private record CardHoldingPair(Card c, boolean m) {
        @Override
        public String toString() {
            return c + (m ? "" : " HELD");
        }
    }
}
