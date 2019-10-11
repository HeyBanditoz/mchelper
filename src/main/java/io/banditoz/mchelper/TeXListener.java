package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.CommandUtils;
import io.banditoz.mchelper.utils.TeXRenderer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                TeXRenderer.sendTeXToChannel(event, latexString);
            } catch (Exception ex) {
                CommandUtils.sendExceptionMessage(event, ex, logger, true);
            }
        }
    }
}
