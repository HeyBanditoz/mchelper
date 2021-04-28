package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EightBallCommand extends Command {
    private final Random random = new SecureRandom();
    private static final List<String> RESPONSES;

    static {
        RESPONSES = new ArrayList<>(20);
        RESPONSES.add("It is certain.");
        RESPONSES.add("It is decidedly so.");
        RESPONSES.add("Without a doubt.");
        RESPONSES.add("Yes – definitely.");
        RESPONSES.add("You may rely on it.");
        RESPONSES.add("As I see it, yes.");
        RESPONSES.add("Most likely.");
        RESPONSES.add("Outlook good.");
        RESPONSES.add("Yes.");
        RESPONSES.add("Signs point to yes.");
        RESPONSES.add("Reply hazy, try again.");
        RESPONSES.add("Ask again later.");
        RESPONSES.add("Better not tell you now.");
        RESPONSES.add("Cannot predict now.");
        RESPONSES.add("Concentrate and ask again.");
        RESPONSES.add("Don't count on it.");
        RESPONSES.add("My reply is no.");
        RESPONSES.add("My sources say no.");
        RESPONSES.add("Outlook not so good.");
        RESPONSES.add("Very doubtful.");
    }

    @Override
    public String commandName() {
        return "8";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Seek wisdom of the eight ball.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        // if they're from a guild, get their (maybe existent) nickname, else just their username
        String name = (ce.getEvent().isFromGuild() ? ce.getEvent().getMember().getEffectiveName() : ce.getEvent().getAuthor().getName());
        int num = random.nextInt(RESPONSES.size());

        ce.sendEmbedReply(new EmbedBuilder()
                .setAuthor(name + " shakes the magic 8-ball, asking...", null, ce.getEvent().getAuthor().getEffectiveAvatarUrl())
                .setDescription(ce.getCommandArgsString())
                .addField("...and the ball responds...", RESPONSES.get(num), true)
                .build());
        return Status.SUCCESS;
    }
}
