package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class CooldownsCommand extends Command {
    @Override
    public String commandName() {
        return "cooldowns";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withDescription("Returns the cooldowns for you or another user.")
                .withParameters("[user as mention]");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        List<User> mentionedUsers = ce.getMentionedUsers();
        User u;
        if (mentionedUsers.isEmpty()) {
            u = ce.getEvent().getAuthor();
        }
        else {
            u = mentionedUsers.get(0);
        }

        StringBuilder sb = new StringBuilder("```\n");
        for (Command command : ce.getMCHelper().getCommandHandler().getCommands()) {
            if (command.getCooldown() != null) {
                switch (command.getCooldown().getType()) {
                    case PER_GUILD:
                        // ensure we're running this from a guild
                        if (ce.getGuild() != null) {
                            // check if the member is in the guild's member cache
                            if (ce.getEvent().getGuild().getMember(u) != null) {
                                if (command.getCooldown().isOnCooldown(ce.getGuild())) {
                                    sb.append("[Guild] ").append(command.commandName()).append(": ").append(command.getCooldown().getRemainingTime(ce.getGuild())).append('\n');
                                }
                            }
                        }
                        break;
                    case PER_USER:
                        if (command.getCooldown().isOnCooldown(u)) {
                            sb.append("[User] ").append(command.commandName()).append(": ").append(command.getCooldown().getRemainingTime(u)).append('\n');
                        }
                        break;
                }
            }
        }
        if (sb.toString().equals("```\n")) {
            ce.sendReply("You are not on cooldown for any command.");
        }
        else {
            ce.sendReply(sb.toString() + "```");
        }
        return Status.SUCCESS;
    }
}
