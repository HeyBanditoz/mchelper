package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.ReactionRole;
import io.banditoz.mchelper.utils.database.dao.RolesDao;
import io.banditoz.mchelper.utils.database.dao.RolesDaoImpl;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
