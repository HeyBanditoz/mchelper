package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ReactionRole;
import io.banditoz.mchelper.utils.ReactionRoleMessage;
import io.banditoz.mchelper.utils.database.dao.RolesDao;
import io.banditoz.mchelper.utils.database.dao.RolesDaoImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.banditoz.mchelper.utils.RoleReactionUtils.removeRoleAndUpdateMessage;

public class RoleReactionListener extends ListenerAdapter {
    private final MCHelper mcHelper;
    private final RolesDao dao;
    private final Logger LOGGER = LoggerFactory.getLogger(RoleReactionListener.class);

    public RoleReactionListener(MCHelper mcHelper) {
        this.mcHelper = mcHelper;
        this.dao = new RolesDaoImpl(mcHelper.getDatabase());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {
        if (!event.isFromType(ChannelType.TEXT) || event.getUser().isBot()) {
            return;
        }
        mcHelper.getThreadPoolExecutor().execute(() -> {
            try {
                if (dao.getRoleReactions().contains(event.getMessageIdLong())) {
                    ReactionRole r = dao.getByEmote(event.getEmoji(), event.getGuild());
                    if (r == null) {
                        return;
                    }
                    switch (event) {
                        case MessageReactionAddEvent e    -> giveRoleToMember(r.roleId(), e.getMember());
                        case MessageReactionRemoveEvent e -> revokeRoleFromMember(r.roleId(), e.getMember());
                        default -> throw new IllegalStateException("Unexpected value: " + event + " (" + event.getClass() + ")");
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Could not give role to member " + event.getMember() + "!", e);
            }
        });
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        mcHelper.getThreadPoolExecutor().execute(() -> {
            try {
                Emoji removed = dao.removeRole(event.getRole());
                removeRoleAndUpdateMessage(dao, removed, event.getGuild());
            } catch (Exception ex) {
                LOGGER.error("Could not handle role deletion!", ex);
            }
        });
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        mcHelper.getThreadPoolExecutor().execute(() -> {
            try {
                ReactionRoleMessage messageRole = dao.getMessageRole(event.getGuild());
                if (messageRole != null && messageRole.messageId() == event.getMessageIdLong()) {
                    dao.deactivate(event.getGuild());
                }
            } catch (Exception ex) {
                LOGGER.error("Could not deactivate guild!", ex);
            }
        });
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        mcHelper.getThreadPoolExecutor().execute(() -> {
            try {
                ReactionRoleMessage messageRole = dao.getMessageRole(event.getGuild());
                if (messageRole == null) {
                    return;
                }
                for (String messageId : event.getMessageIds()) {
                    long messageIdLong = Long.parseLong(messageId);
                    if (messageRole.messageId() == messageIdLong) {
                        dao.deactivate(event.getGuild());
                        break;
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Could not deactivate guild!", ex);
            }
        });
    }



    private void giveRoleToMember(long roleId, Member m) {
        Guild g = m.getGuild();
        Role role = g.getRoleById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Role by id " + roleId + " doesn't exist in guild " + g);
        }
        g.addRoleToMember(m, role)
                .reason("This member reacted to the roles reaction message, and was granted the requested role.")
                .queue();
    }

    private void revokeRoleFromMember(long roleId, Member m) {
        Guild g = m.getGuild();
        Role role = g.getRoleById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Role by id " + roleId + " doesn't exist in guild " + g);
        }
        g.removeRoleFromMember(m, role)
                .reason("This member removed their reaction to the roles reaction message, and was revoked the requested role.")
                .queue();
    }

    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        // TODO handle this case!
    }
}
