package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.games.GameManager;
import io.banditoz.mchelper.games.VideoPokerGame;
import io.banditoz.mchelper.interactions.ButtonInteractable;
import io.banditoz.mchelper.interactions.InteractionListener;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Singleton
@RequiresDatabase
public class VideoPokerCommand extends Command {
    private final AccountManager am;
    private final GameManager gm;
    private final ScheduledExecutorService ses;
    private final InteractionListener interactionListener;

    @Inject
    public VideoPokerCommand(AccountManager am,
                             GameManager gm,
                             ScheduledExecutorService ses,
                             InteractionListener interactionListener) {
        this.am = am;
        this.gm = gm;
        this.ses = ses;
        this.interactionListener = interactionListener;
    }

    @Override
    public String commandName() {
        return "poker";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<ante (with range 100 <= x <= 500 )>")
                .withDescription("Play video poker to win some money! For the paytable, visit" +
                        " https://en.wikipedia.org/w/index.php?title=Video_poker&oldid=1094389801#Jacks_or_Better");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        User u = ce.getEvent().getAuthor();
        BigDecimal ante = new BigDecimal(ce.getCommandArgs()[1].replace(",", ""));
        VideoPokerGame game = new VideoPokerGame(ante, u, gm, am, ses);
        game.tryAndRemoveAnte("video poker ante");
        game.startPlaying();
        // god has left us
        Button holdOne   = Button.primary(UUID.randomUUID().toString(), "1");
        Button holdTwo   = Button.primary(UUID.randomUUID().toString(), "2");
        Button holdThree = Button.primary(UUID.randomUUID().toString(), "3");
        Button holdFour  = Button.primary(UUID.randomUUID().toString(), "4");
        Button holdFive  = Button.primary(UUID.randomUUID().toString(), "5");
        Button submit    = Button.danger(UUID.randomUUID().toString(), "Submit");
        Button invert    = Button.secondary(UUID.randomUUID().toString(), "Invert");
        Button haas      = Button.danger(UUID.randomUUID().toString(), "Hold All & Submit");
        MessageEmbed messageEmbed = game.generateEmbed();

        MessageCreateData m = new MessageCreateBuilder()
                .setComponents(ActionRow.of(holdOne, holdTwo, holdThree, holdFour, holdFive),
                               ActionRow.of(submit, haas, invert))
                .setEmbeds(messageEmbed)
                .build();
        ce.getEvent().getChannel().sendMessage(m).queue(message -> {
            ButtonInteractable i = new ButtonInteractable(
                    Map.of(holdOne, game::mark,
                           holdTwo, game::mark,
                           holdThree, game::mark,
                           holdFour, game::mark,
                           holdFive, game::mark,
                           submit, game::submit,
                           haas, game::holdAllAndSubmit,
                           invert, game::invert),
                    ce.getEvent().getAuthor()::equals, 0, message, ce);
            interactionListener.addInteractable(i);
        });
        return Status.SUCCESS;
    }
}
