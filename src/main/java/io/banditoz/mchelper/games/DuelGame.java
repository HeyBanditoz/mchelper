package io.banditoz.mchelper.games;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.WrappedButtonClickEvent;
import io.banditoz.mchelper.money.AccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DuelGame extends Game {
    private volatile User opponent;
    private static final Random RANDOM = new Random();
    private boolean complete = false;
    private static final BigDecimal TWO = new BigDecimal("2");

    private static final Logger LOGGER = LoggerFactory.getLogger(DuelGame.class);

    public DuelGame(BigDecimal ante, User player, MCHelper mcHelper) {
        super(5, 200_000, mcHelper, player, ante);
    }

    public void enterGame(WrappedButtonClickEvent event) {
        synchronized (this) {
            if (complete || opponent != null) {
                event.getEvent().deferEdit().queue();
                return;
            }
            if (event.getUser().equals(player)) {
                event.getEvent().deferEdit().queue();
                return;
            }
            try {
                am.remove(ante, event.getUser().getIdLong(), "duel ante with " + player.getIdLong());
            } catch (Exception e) {
                event.getEvent().reply("Error! " + e.getMessage()).setEphemeral(true).queue();
                LOGGER.warn("Error subtracting money from prospecting opponent!", e);
                return;
            }
            opponent = event.getUser();
            event.getEvent().deferEdit().queue();
            MessageEmbed me = new EmbedBuilder()
                    .setTitle("Duel!")
                    .setDescription("Who will click the button first?\n"
                            + player.getAsMention() + " vs. " + opponent.getAsMention() + "!\n"
                            + "There's $" + AccountManager.format(ante) + " on the line!")
                    .setColor(Color.MAGENTA)
                    .build();
            event.getMessage().editMessageEmbeds(me).setActionRows().queue();
            mcHelper.getSES().schedule(() -> {
                Button clickMe = Button.primary(UUID.randomUUID().toString(), "Click Me!");
                ButtonInteractable bi = new ButtonInteractable(
                        Map.of(clickMe, this::fight),
                        user -> user.equals(player) || user.equals(opponent), 0, event.getMessage());
                event.destroyThenReplaceWith(bi, ActionRow.of(clickMe));
            }, RANDOM.nextLong(7 - 2 + 1) + 2, TimeUnit.SECONDS);
        }
    }

    public void fight(WrappedButtonClickEvent event) {
        synchronized (this) {
            event.getEvent().deferEdit().queue();
            // guard so you can't win twice, or someone else
            if (complete) {
                return;
            }
            complete = true;

            // set up whom the winner was actually fighting, for memo purposes
            User realOpponent = event.getUser().equals(opponent) ? player : opponent;
            String win = event.getUser().getAsMention() + " won an extra $" + AccountManager.format(ante);  //+ " from " + realOpponent.getAsMention() + "!";

            event.getEvent().getChannel().sendMessage(win).queue();
            MessageEmbed me = new EmbedBuilder()
                    .setTitle("Duel!")
                    .setDescription(win)
                    .setColor(Color.GREEN)
                    .build();
            event.removeListenerAndDestroy(me);
            try {
                am.add(ante.multiply(TWO), event.getEvent().getUser().getIdLong(), "duel winnings against " + realOpponent.getIdLong());
            } catch (Exception e) {
                LOGGER.error("Error adding money to winner! This shouldn't happen.", e);
            }
            stopPlaying();
        }
    }

    public void cancel(WrappedButtonClickEvent event) {
        synchronized (this) {
            event.getEvent().deferEdit().queue();
            if (!player.equals(event.getUser())) {
                return;
            }
            complete = true;
            try {
                am.add(ante, player.getIdLong(), "duel cancelled");
            } catch (Exception e) {
                LOGGER.error("Error giving money due to cancellation! This shouldn't happen.", e);
            }
            MessageEmbed me = new EmbedBuilder()
                    .setTitle("Duel!")
                    .setDescription("Duel cancelled.")
                    .setColor(Color.RED)
                    .build();
            stopPlaying();
            event.removeListenerAndDestroy(me);
        }
    }
}
