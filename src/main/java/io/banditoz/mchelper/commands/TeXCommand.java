package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TeXRenderer;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.InputStream;

public class TeXCommand extends Command {
    @Override
    public String commandName() {
        return "!tex";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, String[] commandArgs) {
        StringBuilder args = new StringBuilder();
        for (int i = 1; i < commandArgs.length; i++) {
            args.append(commandArgs[i]).append(" ");
        }
        try {
            InputStream latex = TeXRenderer.renderTeX(args.toString());
            e.getMessage().getChannel()
                    .sendMessage("TeX for " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator())
                    .addFile(latex, "latex.png").queue();
        } catch (Exception ex) {
            sendExceptionMessage(e, ex);
        }
    }
}
