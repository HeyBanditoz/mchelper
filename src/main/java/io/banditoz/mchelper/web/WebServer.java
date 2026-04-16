package io.banditoz.mchelper.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.config.Config;
import io.avaje.inject.RequiresProperty;
import io.avaje.jex.Jex;
import io.avaje.jex.core.json.JacksonJsonService;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpFilter;
import io.avaje.jex.http.HttpResponseException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RequiresProperty("mchelper.web.enabled")
public class WebServer implements AutoCloseable {
    private final JDA jda;
    private final Jex.Server server;
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    @Inject
    public WebServer(JDA jda, ObjectMapper objectMapper) {
        this.jda = jda;
        this.server = Jex.create()
                .port(Config.getInt("mchelper.web.port", 9999))
                .jsonService(new JacksonJsonService(objectMapper))
                .filter(this::log)
                .filter(this::auth)
                .post("/messages/threads/{tid}", this::postThreadMessage)
                .start();
    }

    private void postThreadMessage(Context ctx) {
        ThreadChannel thread = jda.getThreadChannelById(ctx.pathParam("tid"));
        if (thread == null) {
            ctx.status(400).text("Thread not found.");
            return;
        }
        if (!thread.isJoined()) {
            ctx.status(400).text("User not in thread.");
            return;
        }
        WebhookMessage message = ctx.bodyAsClass(WebhookMessage.class);
        MessageEmbed me = new EmbedBuilder()
                .setTitle(message.title())
                .setUrl(message.url())
                .setDescription(message.description())
                .build();
        MessageCreateData mcd = new MessageCreateBuilder()
                .setAllowedMentions(null)
                .setEmbeds(me)
                .addContent(message.message())
                .build();
        thread.sendMessage(mcd).complete();
        ctx.status(200).text("Message sent.");
    }

    private void log(Context context, HttpFilter.FilterChain filterChain) {
        long before = System.currentTimeMillis();
        try {
            log.info("Request made url=\"{}\" method={}", context.path(), context.method());
            filterChain.proceed();
        } finally {
            long dur = System.currentTimeMillis() - before;
            log.info("Request complete url=\"{}\" method={} status={} durationMs={}", context.path(), context.method(), context.status(), dur);
        }
    }

    private void auth(Context context, HttpFilter.FilterChain filterChain) {
        if (!Config.get("mchelper.web.key").equals(context.header("Authorization"))) {
            throw new HttpResponseException(403, "Unauthorized");
        }
        filterChain.proceed();
    }

    @Override
    public void close() throws Exception {
        log.info("Shutting down web server...");
        server.shutdown();
        log.info("Web server shutdown");
    }

    private record WebhookMessage(String title, String url, String description, String message) {}

}
