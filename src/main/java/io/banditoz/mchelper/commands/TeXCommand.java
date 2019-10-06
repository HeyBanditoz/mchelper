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
    protected void onCommand() {
        try {
            String imageName = Base64.getEncoder().encodeToString(DigestUtils.md5(commandArgsString)) + ".png";
            long before = System.currentTimeMillis();
            ByteArrayOutputStream latex = TeXRenderer.renderTeX(commandArgsString);
            long after = System.currentTimeMillis() - before;
            e.getMessage().getChannel()
                    .sendMessage("TeX for " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " (took " + after + " ms to generate)")
                    .addFile(new ByteArrayInputStream(latex.toByteArray()), imageName)
                    .queue();
            latex.close();
        } catch (Exception ex) {
            sendExceptionMessage(ex);
}    }
}
