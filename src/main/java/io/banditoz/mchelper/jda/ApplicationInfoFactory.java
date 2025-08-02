package io.banditoz.mchelper.jda;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationInfo;

@Factory
public class ApplicationInfoFactory {
    private final JDA jda;

    @Inject
    public ApplicationInfoFactory(JDA jda) {
        this.jda = jda;
    }

    @Bean
    public ApplicationInfo applicationInfo() {
        return jda.retrieveApplicationInfo().complete();
    }
}
