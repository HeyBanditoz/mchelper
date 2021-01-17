package io.banditoz.mchelper.utils;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.database.dao.RolesDao;
import io.banditoz.mchelper.utils.database.dao.RolesDaoImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RoleReactionListener {
    private HashMap<String, String> messages = new HashMap<>();
    private RolesDao rd;
    private MCHelper mcHelper;
    private final Logger LOGGER = LoggerFactory.getLogger(RoleReactionListener.class);
    private final EventHandler<MessageReactionAddEvent> addHandler = new EventHandler<MessageReactionAddEvent>() {
        @Override
        public void eventConsumer(GenericEvent event) {
            MessageReactionAddEvent messageReactionAddEvent = (MessageReactionAddEvent)event;
            if (messageReactionAddEvent.getUser().isBot()) {
                return;
            }
            if (!messages.containsValue(messageReactionAddEvent.getMessageId())) {
                return;
            }
            try {
                if (events.containsKey(":" + messageReactionAddEvent.getReactionEmote().getEmote().getName() + ":")) {
                    events.get(":" + messageReactionAddEvent.getReactionEmote().getEmote().getName() + ":").accept(messageReactionAddEvent);
                } else {
                    messageReactionAddEvent.retrieveMessage().queue(message -> {message.removeReaction(messageReactionAddEvent.getReactionEmote().getEmote(),messageReactionAddEvent.getUser()).queue();});
                }
            } catch (IllegalStateException ex) {
                if (events.containsKey(messageReactionAddEvent.getReactionEmote().getName())) {
                    events.get(messageReactionAddEvent.getReactionEmote().getName()).accept(messageReactionAddEvent);
                } else {
                    messageReactionAddEvent.retrieveMessage().queue(message -> {message.removeReaction(messageReactionAddEvent.getReactionEmote().getAsReactionCode(), messageReactionAddEvent.getUser()).queue();});
                }
            }
        }
    };
    private final EventHandler<MessageReactionRemoveEvent> removeHandler = new EventHandler<MessageReactionRemoveEvent>() {
        @Override
        public void eventConsumer(GenericEvent event) {
            MessageReactionRemoveEvent messageReactionRemoveEvent = (MessageReactionRemoveEvent)event;
            if (messageReactionRemoveEvent.getUser().isBot()) {
                return;
            }
            if (!messages.containsValue(messageReactionRemoveEvent.getMessageId())) {
                return;
            }
            try {
                if (events.containsKey(":" + messageReactionRemoveEvent.getReactionEmote().getEmote().getName() + ":")) {
                    events.get(":" + messageReactionRemoveEvent.getReactionEmote().getEmote().getName() + ":").accept(messageReactionRemoveEvent);
                }
            } catch (IllegalStateException ex) {
                if (events.containsKey(messageReactionRemoveEvent.getReactionEmote().getName())) {
                    events.get(messageReactionRemoveEvent.getReactionEmote().getName()).accept(messageReactionRemoveEvent);
                }
            }
        }
    };

    public RoleReactionListener(MCHelper mcHelper) {
        this.mcHelper = mcHelper;
        this.rd = new RolesDaoImpl(mcHelper.getDatabase());
        mcHelper.getJDA().addEventListener(addHandler);
        mcHelper.getJDA().addEventListener(removeHandler);

        for (Guild g : mcHelper.getJDA().getGuilds()) {
            try {
                if (!rd.containsGuild(g)) {
                    continue;
                }
                Map.Entry<String, String> temp = rd.getChannelAndMessageId(g);
                messages.put(temp.getKey(),temp.getValue());
                List<RoleObject> map = rd.getRoles(g);
                for (RoleObject e : map) {
                    addHandler.addEvent(e.getEmote(), messageReactionAddEvent -> {
                        if (mcHelper.getJDA().getSelfUser().getId().equals(messageReactionAddEvent.getUserId())) {
                            return;
                        }
                        try {
                            try {
                                messageReactionAddEvent.getGuild().addRoleToMember(messageReactionAddEvent.getUserId(), messageReactionAddEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionAddEvent.getGuild(), ":" + messageReactionAddEvent.getReactionEmote().getEmote().getName() + ":").getRole_id())).queue();
                            } catch (IllegalStateException ex) {
                                messageReactionAddEvent.getGuild().addRoleToMember(messageReactionAddEvent.getUserId(), messageReactionAddEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionAddEvent.getGuild(), messageReactionAddEvent.getReactionEmote().getName()).getRole_id())).queue();
                            }
                        } catch (SQLException ex) {
                            LOGGER.error("Error connecting to database!",ex);
                        }
                    });
                    removeHandler.addEvent(e.getEmote(), messageReactionRemoveEvent -> {
                        if (mcHelper.getJDA().getSelfUser().getId().equals(messageReactionRemoveEvent.getUserId())) {
                            return;
                        }
                        try {
                            try {
                                messageReactionRemoveEvent.getGuild().removeRoleFromMember(messageReactionRemoveEvent.getUserId(), messageReactionRemoveEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionRemoveEvent.getGuild(), ":" + messageReactionRemoveEvent.getReactionEmote().getEmote().getName() + ":").getRole_id())).queue();
                            } catch (IllegalStateException ex) {
                                messageReactionRemoveEvent.getGuild().removeRoleFromMember(messageReactionRemoveEvent.getUserId(), messageReactionRemoveEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionRemoveEvent.getGuild(), messageReactionRemoveEvent.getReactionEmote().getName()).getRole_id())).queue();
                            }
                        } catch (SQLException ex) {
                            LOGGER.error("Error connecting to database!",ex);
                        }
                    });
                }
            } catch (SQLException throwables) {
                LOGGER.error("Error connecting to database!",throwables);
            }
        }
    }

    public void addEvent(String emote) {
        addHandler.addEvent(emote, messageReactionAddEvent -> {
            if (mcHelper.getJDA().getSelfUser().getId().equals(messageReactionAddEvent.getUserId())) {
                return;
            }
            try {
                try {
                    messageReactionAddEvent.getGuild().addRoleToMember(messageReactionAddEvent.getUserId(), messageReactionAddEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionAddEvent.getGuild(), ":" + messageReactionAddEvent.getReactionEmote().getEmote().getName() + ":").getRole_id())).queue();
                } catch (IllegalStateException ex) {
                    messageReactionAddEvent.getGuild().addRoleToMember(messageReactionAddEvent.getUserId(), messageReactionAddEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionAddEvent.getGuild(), messageReactionAddEvent.getReactionEmote().getName()).getRole_id())).queue();
                }
            } catch (SQLException ex) {
                LOGGER.error("Error connecting to database!",ex);
            }
        });
        removeHandler.addEvent(emote, messageReactionRemoveEvent -> {
            if (mcHelper.getJDA().getSelfUser().getId().equals(messageReactionRemoveEvent.getUserId())) {
                return;
            }
            try {
                try {
                    messageReactionRemoveEvent.getGuild().removeRoleFromMember(messageReactionRemoveEvent.getUserId(), messageReactionRemoveEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionRemoveEvent.getGuild(), ":" + messageReactionRemoveEvent.getReactionEmote().getEmote().getName() + ":").getRole_id())).queue();
                } catch (IllegalStateException ex) {
                    messageReactionRemoveEvent.getGuild().removeRoleFromMember(messageReactionRemoveEvent.getUserId(), messageReactionRemoveEvent.getGuild().getRoleById(rd.getRoleByEmote(messageReactionRemoveEvent.getGuild(), messageReactionRemoveEvent.getReactionEmote().getName()).getRole_id())).queue();
                }
            } catch (SQLException ex) {
                LOGGER.error("Error connecting to database!",ex);
            }
        });
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, String> messages) {
        this.messages = messages;
    }

    public void removeEvent(String emote) {
        addHandler.removeEvent(emote);
        removeHandler.removeEvent(emote);
    }

    public EventHandler<MessageReactionAddEvent> getAddHandler() {
        return addHandler;
    }

    public EventHandler<MessageReactionRemoveEvent> getRemoveHandler() {
        return removeHandler;
    }
}