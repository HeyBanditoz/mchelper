package io.banditoz.mchelper.commands;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class TTT extends Command {
    Random random = new Random();

    @Override
    public String commandName() {
        return "!ttt";
    }

    @Override
    protected void onCommand() {
        int howMany = random.nextInt(4);
        if (howMany == 0)
            howMany++;
        if (commandArgsString.contains(",")) {
            ArrayList<String> options = new ArrayList<>(Arrays.asList(commandArgsString.split("\\s+,\\s+")));
            String players = extractNumRandomly(howMany, options);
            if (players.contains("walker") == true || players.contains("Taylor") == true || players.contains("taylor") == true)
            {
                sendPrivateMessage(e.getJDA().getUserById("440702753380237312"),"TRAITOR!!");
            }
            if (players.contains("bandit") == true || players.contains("Hayden") == true || players.contains("hayden"))
            {
                sendPrivateMessage(e.getJDA().getUserById("163094867910590464"),"TRAITOR!!");
                sendReply("<@163094867910590464>");
            }
            if (players.contains("josh") == true || players.contains("Superskish") == true || players.contains("superskish") == true)
            {
                sendPrivateMessage(e.getJDA().getUserById("375040836829839360"),"TRAITOR!!");
                sendReply("<@375040836829839360>");
            }
            if (players.contains("Kadin") == true || players.contains("kanislupus") == true || players.contains("Kanislupus") == true || players.contains("kadin") == true)
            {
                sendPrivateMessage(e.getJDA().getUserById("384834373012160523"),"TRAITOR!!");
                sendReply("<@384834373012160523>");
            }
            if (players.contains("ashton") == true || players.contains("Ashton") == true || players.contains("xradusa") == true || players.contains("Xradiusa") == true)
            {
                sendPrivateMessage(e.getJDA().getUserById("226880247692394496"),"TRAITOR!!");
                sendReply("<@226880247692394496>");
            }
            if (players.contains("mccord") == true || players.contains("McCord") == true || players.contains("xradusm") == true || players.contains("Xradiusm") == true)
            {
                sendPrivateMessage(e.getJDA().getUserById("339820351376850944"),"TRAITOR!!");
                sendReply("<@339820351376850944>");
            }
            if (players.contains("kyler") == true || players.contains("Kyler") == true || players.contains("EvelerKyvans") || players.contains("Eveler Kyvans"))
            {
                sendPrivateMessage(e.getJDA().getUserById("404837963697225729"),"TRAITOR!!");
                sendReply("<@404837963697225729>");
            }
        } else
            sendReply("**Unknown error.... Exiting**");
    }

    private String extractNumRandomly(int num, ArrayList<String> l) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int pos = random.nextInt(l.size());
            results.append(l.get(pos));
            if (i < num - 1) {
                results.append(", ");
            }
            l.remove(pos);
        }
        String s = results.toString();
        return s;
    }
    public void sendPrivateMessage(User user, String content) {
        // openPrivateChannel provides a RestAction<PrivateChannel>
        // which means it supplies you with the resulting channel
        user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(content).queue();
        });
    }
}