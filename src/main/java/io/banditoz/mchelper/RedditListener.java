package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ExtractRedditLink;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Matcher m = Pattern.compile("https://reddit.app.link/\\w.*").matcher(event.getMessage().getContentDisplay());
        try {
            if (m.find()) {
                event.getChannel().sendMessage(ExtractRedditLink.extractFromRedditAppLink(m.group())).queue();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
