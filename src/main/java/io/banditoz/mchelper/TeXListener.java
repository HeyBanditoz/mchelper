package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.TeXRenderer;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.banditoz.mchelper.commands.Command.sendExceptionMessage;


public class TeXListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Matcher m = Pattern.compile("\\$\\$(.*?)\\$\\$").matcher(event.getMessage().getContentDisplay());
        if (m.find()) {
            event.getChannel().sendTyping().queue();
            String latexString = m.group(1);
            try {
                long before = System.currentTimeMillis();
                ByteArrayOutputStream latex = TeXRenderer.renderTeX(latexString);
                long after = System.currentTimeMillis() - before;
                String imageName = Base64.getEncoder().encodeToString(DigestUtils.md5(latexString)) + ".png";
                event.getMessage().getChannel()
                        .sendMessage("TeX for " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (took " + after + " ms to generate)")
                        .addFile(new ByteArrayInputStream(latex.toByteArray()), imageName)
                        .queue();
                latex.close();
            } catch (Exception ex) {
                sendExceptionMessage(event, ex);
            }
        }
    }
}
