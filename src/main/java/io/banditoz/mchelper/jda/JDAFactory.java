package io.banditoz.mchelper.jda;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class JDAFactory implements AutoCloseable {
    private final JDA jda;
    private static final Logger log = LoggerFactory.getLogger(JDAFactory.class);

    public JDAFactory() {
        try {
            this.jda = JDABuilder.createDefault(Config.get("mchelper.discord.token"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EXPRESSIONS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMaxReconnectDelay(32)
                    .setEnableShutdownHook(false)
                    .build()
                    .awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public JDA jda() {
        return jda;
    }

    @Override
    public void close() throws Exception {
        log.info("Shutting down JDA...");
        jda.shutdown();
    }
}
