package io.banditoz.mchelper;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.banditoz.mchelper.stats.StatsRecorder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MessageLogger extends ListenerAdapter {
    private final MCHelper MCHELPER;
    private final ExecutorService ES;
    private final Logger LOGGER = LoggerFactory.getLogger(StatsRecorder.class);

    public MessageLogger(MCHelper mcHelper, ExecutorService es) {
        this.MCHELPER = mcHelper;
        this.ES = es;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (String loggedChannel : MCHELPER.getSettings().getLoggedChannels()) {
            if (event.getChannel().getId().equals(loggedChannel)) {
                log(event.getMessage());
                break; // found a match, don't keep iterating
            }
        }
    }

    private void log(Message m) {
        ES.execute(() -> {
            try {
                String json = MCHELPER.getObjectMapper().writeValueAsString(new LoggableMessage(m));
                Request request = new Request.Builder()
                        .url(MCHELPER.getSettings().getElasticsearchMessageEndpoint())
                        .post(RequestBody.create(MediaType.get("application/json"), json))
                        .build();
                MCHELPER.performHttpRequestIgnoreResponse(request);
            } catch (Exception ex) {
                LOGGER.error("Error while saving message " + m.getIdLong() + " to Elasticsearch!", ex);
            }
        });
    }

    @SuppressWarnings("unused")
    private static class LoggableMessage {
        private final long channelId;
        private final long messageId;
        private final long authorId;
        private final String authorAndDiscrim;
        private final List<String> attachments;
        private final String messageContent;
        @JsonProperty("@timestamp")
        private final long time;
        private final boolean hasEmbed;
        private final boolean isBot;

        public LoggableMessage(Message m) {
            this.authorId = m.getIdLong();
            this.messageContent = m.getContentRaw();
            this.authorAndDiscrim = m.getAuthor().getAsTag();
            this.time = m.getTimeCreated().toInstant().toEpochMilli();
            this.hasEmbed = !m.getEmbeds().isEmpty();
            this.isBot = m.getAuthor().isBot();
            this.channelId = m.getChannel().getIdLong();
            this.messageId = m.getIdLong();
            if (!m.getAttachments().isEmpty()) {
                this.attachments = new ArrayList<>(m.getAttachments().size());
                for (Message.Attachment attachment : m.getAttachments()) {
                    attachments.add(attachment.getUrl());
                }
            }
            else {
                this.attachments = Collections.emptyList();
            }
        }

        public long getChannelId() {
            return channelId;
        }

        public long getMessageId() {
            return messageId;
        }

        public long getAuthorId() {
            return authorId;
        }

        public String getAuthorAndDiscrim() {
            return authorAndDiscrim;
        }

        public List<String> getAttachments() {
            return attachments;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public long getTime() {
            return time;
        }

        public boolean getHasEmbed() {
            return hasEmbed;
        }

        public boolean getIsBot() {
            return isBot;
        }
    }
}
