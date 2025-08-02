package io.banditoz.mchelper.games;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleOrNothingGame extends Game {
    private BigDecimal currentBet;
    private final Random rand = new Random();
    private int times = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleOrNothingGame.class);

    public DoubleOrNothingGame(BigDecimal initialBet, User player, GameManager gm, AccountManager am, ScheduledExecutorService ses) {
        super(5, 200_000, player, initialBet, gm, am, ses);
        this.currentBet = initialBet;
    }

    /**
     * Play double or nothing.
     *
     * @return True if they doubled, false if they lost it all!
     */
    public boolean play() {
        if (rand.nextDouble() <= 0.51) {
            currentBet = currentBet.multiply(TWO); // fairly double your money (51% statistically,) but maybe not so in the future?
            times++;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method called when a user intends to play for more money.
     */
    public void bet(WrappedButtonClickEvent wrappedEvent) {
        ButtonInteractionEvent event = wrappedEvent.getEvent();

        if (play()) {
            event.editMessageEmbeds(generate(currentBet , player)).queue();
        }
        else {
            wrappedEvent.destroyThenAddReplayer(lose(currentBet , player));
            gm.stopPlaying(player);
        }
    }

    /**
     * Method called when a user intends to cash out their winnings.
     */
    public void stop(WrappedButtonClickEvent wrappedEvent) {
        stopPlaying();
        // TODO better exception handling
        try {
            payout();
        } catch (Exception ex) {
            LOGGER.error("Error while paying out!", ex);
        } finally {
            wrappedEvent.destroyThenAddReplayer(cashout(currentBet , player));
        }
    }

    public void payout() throws Exception {
        add(currentBet, player.getIdLong(), "double or nothing winnings (bet x" + times + ")");
    }

    public MessageEmbed generate(BigDecimal currentAmount, User u) {
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
