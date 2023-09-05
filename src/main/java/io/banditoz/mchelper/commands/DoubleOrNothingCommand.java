package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.games.DoubleOrNothingGame;
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
public class DoubleOrNothingCommand extends Command {
    private static final BigDecimal LOWER = BigDecimal.valueOf(5);
    private static final BigDecimal UPPER = BigDecimal.valueOf(200000);

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
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1].replace(",", ""));
        // shall we play a game?
        DoubleOrNothingGame game = new DoubleOrNothingGame(ante, u, ce.getMCHelper());
        game.tryAndRemoveAnte("double or nothing ante");
        game.startPlaying();
        MessageEmbed embed = game.generate(ante, u);
        Button bet = Button.primary(UUID.randomUUID().toString(), "Bet");
        Button stop = Button.danger(UUID.randomUUID().toString(), "Stop");
        MessageCreateData m = new MessageCreateBuilder().setActionRow(bet, stop).setEmbeds(embed).build();
        ce.getEvent().getChannel().sendMessage(m).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(bet, game::bet, stop, game::stop),
                    ce.getEvent().getAuthor()::equals, 0, message, ce);
            ce.getMCHelper().getButtonListener().addInteractable(i);
        });
        return Status.SUCCESS;
    }

}
