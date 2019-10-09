package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.TeXRenderer;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            long before = System.nanoTime();
            ByteArrayOutputStream latex = TeXRenderer.renderTeX(commandArgsString);
            long after = System.nanoTime() - before;
            e.getMessage().getChannel()
                    .sendMessage("TeX for " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " (took " + (after / 1000000) + " ms to generate)")
                    .addFile(new ByteArrayInputStream(latex.toByteArray()), imageName)
                    .queue();
            latex.close();
        } catch (IOException ex) {
            sendExceptionMessage(ex);
        }
    }
}
