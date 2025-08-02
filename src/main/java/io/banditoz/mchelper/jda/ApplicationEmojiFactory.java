package io.banditoz.mchelper.jda;

import java.util.List;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;

@Factory
public class ApplicationEmojiFactory {
    private final JDA jda;

    @Inject
    public ApplicationEmojiFactory(JDA jda) {
        this.jda = jda;
    }

    @Bean
    public List<ApplicationEmoji> applicationEmojis() {
        return jda.retrieveApplicationEmojis().complete();
    }
}
