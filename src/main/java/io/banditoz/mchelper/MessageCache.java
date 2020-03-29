package io.banditoz.mchelper;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class MessageCache implements EventListener {
    private final JDA api;
    private final Cache<String, Message> messageMap;

    /**
     * Message Caching for JDA v4. Adapted from
     * https://gist.github.com/Almighty-Alpaca/32629893e9cd305f1165652c80726b41 Used
     * for deleted message caching :)
     * @author https://gist.github.com/Jg99/86f026fbc4dba80408e5e2c915d62b67/04bb8985b3a75faa13813fc73660d123b2469ed5
     */
    public MessageCache(final JDA api) {
        this.api = api;
        this.messageMap = new Cache2kBuilder<String, Message>() {}
                .expireAfterWrite(1, TimeUnit.DAYS)
                .suppressExceptions(false)
                .build();
    }

    public void clear() {
        this.messageMap.clear();
    }

    public int getSize() {
        return this.messageMap.asMap().size();
    }

    public Collection<Message> getCachedMessages() {
        return this.messageMap.asMap().values();
    }

    public RestAction<Message> getMessage(final MessageChannel channel, final String Id) {
        final Message message = this.getMessage(Id);

        if (message == null)
            return channel.retrieveMessageById(Id);
        else
            return new CompletedRestAction<>(api, message);
    }

    public Message getMessage(final String Id) {
        return this.messageMap.asMap().get(Id);
    }

    public RestAction<Message> getMessage(final String channelId, final String Id) {
        final Message message = this.getMessage(Id);

        if (message == null) {
            MessageChannel channel = this.api.getTextChannelById(channelId);

            if (channel == null) {
                channel = this.api.getPrivateChannelById(channelId);
            }
            if (channel != null) {
                return channel.retrieveMessageById(Id);
            }
        }
        return new CompletedRestAction<>(api, message);
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            final Message message = ((MessageReceivedEvent) event).getMessage();
            this.messageMap.put(message.getId(), message);
        }
        /*
        if (event instanceof MessageDeleteEvent) {
            this.messageMap.asMap().remove(((MessageDeleteEvent) event).getMessageId());
        }
        if (event instanceof MessageBulkDeleteEvent) {
            this.messageMap.asMap().keySet().removeAll(((MessageBulkDeleteEvent) event).getMessageIds());
        }
        if (event instanceof MessageUpdateEvent) {
            final Message message = ((MessageUpdateEvent) event).getMessage();
            this.messageMap.put(message.getId(), message);
        }
        */
    }
}