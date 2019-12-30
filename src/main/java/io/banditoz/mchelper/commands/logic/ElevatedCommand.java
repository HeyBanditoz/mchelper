package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandUtils;
import io.banditoz.mchelper.commands.logic.CommandPermissions;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the abstract class for any command that only bot maintainers can run.
 *
 * @see Command
 */
public abstract class ElevatedCommand extends Command {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (containsCommand(e)) {
            if (CommandPermissions.isBotOwner(e.getAuthor())) {
                this.e = e;
                go();
            }
            else {
                CommandUtils.sendReply(String.format("User %s (ID: %s) does not have permission to run this command!", e.getAuthor().getAsTag(), e.getAuthor().getId()), e);
            }
        }
    }
}
