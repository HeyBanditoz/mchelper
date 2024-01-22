package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.commands.logic.ElevatedCommand;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessagePurgerCommand extends ElevatedCommand {
    private static final Logger log = LoggerFactory.getLogger(MessagePurgerCommand.class);

    @Override
    public String commandName() {
        return "purge";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), true).withParser(getDefualtArgs());
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        Namespace args = getDefualtArgs().parseArgs(ce.getCommandArgsWithoutName());
        String[] split = args.getString("message_ids").split(",");
        List<String> authors = Arrays.asList(args.getString("authors") == null ? new String[] {} : args.getString("authors").split(","));
        long beginning = Long.parseLong(split[0]);
        long end = Long.parseLong(split[1]);
        int removed = 0;

        GuildMessageChannel channel = args.getString("channel") == null ?
                ce.getEvent().getChannel().asGuildMessageChannel() :
                ce.getMCHelper().getJDA().getTextChannelById(args.getString("channel"));

        List<Message> messagesToDelete = new ArrayList<>();
        // should probably rewrite this to just be async...
        for (Message message : channel.getIterableHistory()) {
            if (message.getIdLong() < end) {
                break;
            }
            if (message.getIdLong() <= beginning || message.getIdLong() <= end) {
                if (authors.isEmpty()) {
                    messagesToDelete.add(message);
                }
                else if (authors.contains(message.getAuthor().getId())) {
                    messagesToDelete.add(message);
                }
            }
            if (messagesToDelete.size() == 100) {
                for (CompletableFuture<Void> job : channel.purgeMessages(messagesToDelete)) {
                    job.get(); // block to track timing
                    removed++;
                }
                removed += messagesToDelete.size();
                messagesToDelete = new ArrayList<>();
            }
        }
        if (messagesToDelete.size() == 1) {
            channel.deleteMessageById(messagesToDelete.getFirst().getId()).complete();
            removed++;
        }
        else {
            for (CompletableFuture<Void> job : channel.purgeMessages(messagesToDelete)) {
                job.get(); // block to track timing
                removed += messagesToDelete.size();
            }
        }
        ce.sendReply("Removed " + removed + " messages.");
        log.info("Removed {} messages from {} filtering by authors {}.", removed, channel, authors);
        return Status.SUCCESS;
    }

    private ArgumentParser getDefualtArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("purge").addHelp(false).build();
        parser.description("Purges message between (inclusive) by two messages IDs. There won't be any message " +
                "existence checks, just if the message falls between the two provided IDs. Be careful with this! There " +
                "are no guardrails.");
        parser.addArgument("-m", "--message-ids")
                .type(String.class)
                .help("removes all messages between and including the IDs. comma separate, i.e. first,last");
        parser.addArgument("-a", "--authors")
                .type(String.class)
                .help("only remove authors from this comma-separated user ID list");
        parser.addArgument("-c", "--channel")
                .type(String.class)
                .help("remove messages in this channel, if not provided current channel will be used")
                .setDefault((String) null);

        return parser;
    }
}
