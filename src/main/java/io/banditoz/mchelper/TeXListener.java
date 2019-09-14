package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.CommandUtils;
import io.banditoz.mchelper.utils.TeXRenderer;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeXListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Matcher m = Pattern.compile("\\$\\$(.*?)\\$\\$").matcher(event.getMessage().getContentDisplay());
        if (m.find()) {
            String latexString = m.group(1);
            try {
                ByteArrayOutputStream latex = TeXRenderer.renderTeX(latexString);
                String imageName = Base64.getEncoder().encodeToString(DigestUtils.md5(latexString)) + ".png";
                event.getMessage().getChannel()
                        .sendMessage("TeX for " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator())
                        .addFile(new ByteArrayInputStream(latex.toByteArray()), imageName)
                        .queue();
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(event, ex);
            }
        }
    }
}
