package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.games.BlackJackGame;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Requires(database = true)
public class BlackJackCommand extends Command {
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);

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
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1].replace(",", ""));
        BlackJackGame game = new BlackJackGame(u, ante, ce.getMCHelper());
        game.tryAndRemoveAnte("blackjack ante");
        game.startPlaying();
        // yeah, I know it's weird, startPlaying adds the game to the game list, and play deals the cards
        game.play();
        // resolve instant wins
        if (game.getPlayersSum() == 21) {
            try {
                game.payout(true);
                ce.sendEmbedReply(game.win(BlackJackGame.WinState.BLACKJACK));
                game.stopPlaying();
                return Status.SUCCESS;
            } catch (Exception ex) {
                LOGGER.error("Error while paying out!", ex);
            }
        }
        else if (game.getDealerHand().get(1).rank().getValue() == 1 && game.getDealerSum() == 21) {
            ce.sendEmbedReply(game.lose());
            game.stopPlaying();
            return Status.SUCCESS;
        }
        MessageEmbed embed = game.generate();
        Button hit = Button.primary(UUID.randomUUID().toString(), "Hit");
        Button stay = Button.primary(UUID.randomUUID().toString(), "Stay");
        MessageCreateData m = new MessageCreateBuilder().setEmbeds(embed).addActionRow(hit, stay).build();
        ce.getEvent().getChannel().sendMessage(m).queue(success -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(hit, game::hit, stay, game::stand),
                    ce.getEvent().getAuthor()::equals,
                    0, success, ce);
            ce.getMCHelper().getButtonListener().addInteractable(i);
        });

        return Status.SUCCESS;
    }
}
