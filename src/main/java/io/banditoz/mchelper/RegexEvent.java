package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexEvent extends CommandEvent {
    private Matcher m;

    public RegexEvent(@NotNull MessageReceivedEvent e, Logger logger, String regex) {
        super(e, logger);
        m = Pattern.compile(regex, Pattern.DOTALL).matcher(e.getMessage().getContentDisplay());
    }

    public Matcher getMatcher() {
        return m;
    }
}
