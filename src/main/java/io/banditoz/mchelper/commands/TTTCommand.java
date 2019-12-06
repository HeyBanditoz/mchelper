package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.ListUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

public class TTTCommand extends Command {
    private SecureRandom random = new SecureRandom();

    @Override
    public String commandName() {
        return "!ttt";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("???")
                .withDescription("???");
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        MessageReceivedEvent e = ce.getEvent();
        int howMany = random.nextInt(4);
        if (howMany == 0)
            howMany++;
        if (ce.getCommandArgsString().contains(",")) {
            ArrayList<String> options = new ArrayList<>(Arrays.asList(ce.getCommandArgsString().split("\\s+,\\s+")));
            String players = ListUtils.extractNumRandomly(howMany, options);
            if (players.contains("walker") || players.contains("Taylor") || players.contains("taylor"))
            {
                sendPrivateMessage(e.getJDA().getUserById("440702753380237312"),"TRAITOR!!");
            }
            if (players.contains("bandit") || players.contains("Hayden") || players.contains("hayden"))
            {
                sendPrivateMessage(e.getJDA().getUserById("163094867910590464"),"TRAITOR!!");
                ce.sendReply("<@163094867910590464>");
            }
            if (players.contains("josh") || players.contains("Superskish") || players.contains("superskish"))
            {
                sendPrivateMessage(e.getJDA().getUserById("375040836829839360"),"TRAITOR!!");
                ce.sendReply("<@375040836829839360>");
            }
            if (players.contains("Kadin") || players.contains("kanislupus") || players.contains("Kanislupus") || players.contains("kadin"))
            {
                sendPrivateMessage(e.getJDA().getUserById("384834373012160523"),"TRAITOR!!");
                ce.sendReply("<@384834373012160523>");
            }
            if (players.contains("ashton") || players.contains("Ashton") || players.contains("xradusa") || players.contains("Xradiusa"))
            {
                sendPrivateMessage(e.getJDA().getUserById("226880247692394496"),"TRAITOR!!");
                ce.sendReply("<@226880247692394496>");
            }
            if (players.contains("mccord") || players.contains("McCord") || players.contains("xradusm") || players.contains("Xradiusm"))
            {
                sendPrivateMessage(e.getJDA().getUserById("339820351376850944"),"TRAITOR!!");
                ce.sendReply("<@339820351376850944>");
            }
            if (players.contains("kyler") || players.contains("Kyler") || players.contains("EvelerKyvans") || players.contains("Eveler Kyvans"))
            {
                sendPrivateMessage(e.getJDA().getUserById("404837963697225729"),"TRAITOR!!");
                ce.sendReply("<@404837963697225729>");
            }
        } else
            ce.sendReply("No valid users found");
    }

    private void sendPrivateMessage(User user, String content) {
        // openPrivateChannel provides a RestAction<PrivateChannel>
        // which means it supplies you with the resulting channel
        user.openPrivateChannel().queue((channel) -> channel.sendMessage(content).queue());
    }
}