package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.CommandUtils;
import io.banditoz.mchelper.utils.TeXRenderer;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeXListener extends ListenerAdapter {
    private static Logger logger = LoggerFactory.getLogger(TeXListener.class);
    private static Pattern pattern = Pattern.compile("\\$\\$(.*?)\\$\\$");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Matcher m = pattern.matcher(event.getMessage().getContentDisplay());
        if (m.find()) {
            event.getChannel().sendTyping().queue();
            String latexString = m.group(1);
            try {
                long before = System.currentTimeMillis();
                ByteArrayOutputStream latex = TeXRenderer.renderTeX(latexString);
                long after = System.currentTimeMillis() - before;
                String imageName = DigestUtils.md5Hex(latexString) + ".png";
                event.getMessage().getChannel()
                        .sendMessage("TeX for " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (took " + after + " ms to generate)")
                        .addFile(new ByteArrayInputStream(latex.toByteArray()), imageName)
                        .queue();
                latex.close();
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(event, ex, logger, true);
            }
        }
    }
}
