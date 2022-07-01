package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.Requires;
import io.banditoz.mchelper.games.DuelGame;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Requires(database = true)
public class DuelCommand extends Command {
    private final static Emoji CHECK = Emoji.fromUnicode("\u2611\uFE0F");
    private final static Emoji CANCEL = Emoji.fromUnicode("⛔");


    @Override
    public String commandName() {
        return "duel";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<ante>")
                .withDescription("Duel with your channel for money!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        // don't run from DM, TODO do annotation to guard for this instead?
        if (ce.getChannelType() == ChannelType.PRIVATE) {
            ce.sendReply("You cannot run this from a DM!");
            return Status.FAIL;
        }
        User u = ce.getEvent().getAuthor();
        BigDecimal ante = new BigDecimal(ce.getCommandArgsString().replace(",", ""));
        DuelGame game = new DuelGame(ante, u, ce.getMCHelper());
        game.tryAndRemoveAnte("duel ante");
        game.startPlaying();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Duel!")
                .setDescription("Enter a duel with " + u.getAsMention() + " for $" + AccountManager.format(ante) + "!")
                .setColor(Color.GREEN)
                .build();
        Button play = Button.primary(UUID.randomUUID().toString(), CHECK);
        Button cancel = Button.danger(UUID.randomUUID().toString(), CANCEL);
        Message m = new MessageBuilder().setActionRows(ActionRow.of(play, cancel)).setEmbeds(embed).build();
        ce.getEvent().getChannel().sendMessage(m).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(play, game::enterGame, cancel, game::cancel),
                    user -> true, 0, message);
            ce.getMCHelper().getButtonListener().addInteractable(i);
        });

        return Status.SUCCESS;
    }
}
