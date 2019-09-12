package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TeXRenderer;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

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
            String imageName = Base64.getEncoder().encodeToString(DigestUtils.md5(args.toString())) + ".png";
            ByteArrayOutputStream latex = TeXRenderer.renderTeX(args.toString());
            e.getMessage().getChannel()
                    .sendMessage("TeX for " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator())
                    .addFile(new ByteArrayInputStream(latex.toByteArray()), imageName)
                    .queue();
            latex.close();
        } catch (Exception ex) {
            sendExceptionMessage(e, ex);
}    }
}
