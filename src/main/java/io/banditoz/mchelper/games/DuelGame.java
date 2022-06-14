package io.banditoz.mchelper.games;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.DuelCommand;
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

public class DuelGame {
    private final User initiator;
    private volatile User opponent;
    private final BigDecimal ante;
    private static final Random RANDOM = new Random();
    private final MCHelper mcHelper;
    private boolean complete = false;
    private static final BigDecimal TWO = new BigDecimal("2");

    private static final Logger LOGGER = LoggerFactory.getLogger(DuelGame.class);

    public DuelGame(User initiator, BigDecimal ante, MCHelper mcHelper) {
        this.initiator = initiator;
        this.ante = ante;
        this.mcHelper = mcHelper;
    }

    public void enterGame(WrappedButtonClickEvent event) {
        synchronized (this) {
            if (complete || opponent != null) {
                event.getEvent().deferEdit().queue();
                return;
            }
            if (event.getUser().equals(initiator)) {
                event.getEvent().deferEdit().queue();
                return;
            }
            try {
                mcHelper.getAccountManager().remove(ante, event.getUser().getIdLong(), "duel ante with " + initiator.getIdLong());
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
                            + initiator.getAsMention() + " vs. " + opponent.getAsMention() + "!\n"
                            + "There's $" + AccountManager.format(ante) + " on the line!")
                    .setColor(Color.MAGENTA)
                    .build();
            event.getMessage().editMessageEmbeds(me).setActionRows().queue();
            mcHelper.getSES().schedule(() -> {
                Button clickMe = Button.primary(UUID.randomUUID().toString(), "Click Me!");
                ButtonInteractable bi = new ButtonInteractable(
                        Map.of(clickMe, this::fight),
                        user -> user.equals(initiator) || user.equals(opponent), 0, event.getMessage());
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
            User realOpponent = event.getUser().equals(opponent) ? initiator : opponent;
            String win = event.getUser().getAsMention() + " won an extra $" + AccountManager.format(ante);  //+ " from " + realOpponent.getAsMention() + "!";

            event.getEvent().getChannel().sendMessage(win).queue();
            MessageEmbed me = new EmbedBuilder()
                    .setTitle("Duel!")
                    .setDescription(win)
                    .setColor(Color.GREEN)
                    .build();
            event.removeListenerAndDestroy(me);
            try {
                mcHelper.getAccountManager().add(ante.multiply(TWO), event.getEvent().getUser().getIdLong(), "duel winnings against " + realOpponent.getIdLong());
            } catch (Exception e) {
                LOGGER.error("Error adding money to winner! This shouldn't happen.", e);
            }
            // TODO I'm planning on fixing this at a later commit. Basically I just want a single Map with active users and
            // games on the MCHelper level (maybe just add methods to thee interface so we don't have to expose the Map?)
            DuelCommand.GAMES.remove(initiator);
        }
    }

    public void cancel(WrappedButtonClickEvent event) {
        synchronized (this) {
            event.getEvent().deferEdit().queue();
            if (!initiator.equals(event.getUser())) {
                return;
            }
            complete = true;
            try {
                mcHelper.getAccountManager().add(ante, initiator.getIdLong(), "duel cancelled");
            } catch (Exception e) {
                LOGGER.error("Error giving money due to cancellation! This shouldn't happen.", e);
            }
            MessageEmbed me = new EmbedBuilder()
                    .setTitle("Duel!")
                    .setDescription("Duel cancelled.")
                    .setColor(Color.RED)
                    .build();
            DuelCommand.GAMES.remove(initiator);
            event.removeListenerAndDestroy(me);
        }
    }
}
