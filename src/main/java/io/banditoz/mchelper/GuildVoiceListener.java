package io.banditoz.mchelper;

import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.GuildConfigurationProvider;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GuildVoiceListener extends ListenerAdapter {
    private final MCHelper mcHelper;
    private static final Logger log = LoggerFactory.getLogger(GuildVoiceListener.class);

    public GuildVoiceListener(MCHelper mcHelper) {
        this.mcHelper = mcHelper;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        GuildConfigurationProvider config = new GuildConfigurationProvider(event.getGuild(), event.getMember().getUser(), mcHelper);
        if (event.getMember().getUser().isBot()) {
            return;
        }
        Guild g = event.getGuild();
        long roleId = Long.parseLong(config.get(Config.VOICE_ROLE_ID));
        Role toChange = g.getRoleById(roleId);
        if (toChange == null) {
            log.debug("There is no role in guild {} by id {}. Skipping role update.", g, roleId);
            return;
        }
        if (!g.getMemberById(mcHelper.getJDA().getSelfUser().getIdLong()).hasPermission(Permission.MANAGE_ROLES)) {
            return;
        }
        if (event.getChannelLeft() != null && event.getChannelJoined() != null) {
            return;
        }
        if (event.getChannelJoined() != null) {
            g.addRoleToMember(event.getMember(), toChange)
                    .reason("This member joined a voice channel, and was granted the assigned voice role.")
                    .queue();
        }
        else if (event.getChannelLeft() != null) {
            g.removeRoleFromMember(event.getMember(), toChange)
                    .reason("This member left a voice channel, and was revoked the assigned voice role.")
                    .queue();
        }
    }

    @Override
    public void onStatusChange(@NotNull StatusChangeEvent event) {
        if (event.getNewStatus() == JDA.Status.CONNECTED) {
            updateAll();
        }
    }

    public void updateAll() {
        log.info("Updating all guild voice rules due to status change...");
        AtomicInteger i = new AtomicInteger();
        Map<Long, String> allGuildsWith;
        try {
            allGuildsWith = new GuildConfigDaoImpl(mcHelper.getDatabase()).getAllGuildsWith(Config.VOICE_ROLE_ID);
        } catch (Exception e) {
            log.error("Encountered Exception while updating all voice roles.", e);
            return;
        }
        allGuildsWith.forEach((guildId, voiceRoleId) -> {
            Guild g = mcHelper.getJDA().getGuildById(guildId);
            if (g == null) {
                return;
            }
            if (!voiceRoleId.equals("0")) {
                if (!g.getMemberById(mcHelper.getJDA().getSelfUser().getIdLong()).hasPermission(Permission.MANAGE_ROLES)) {
                    return;
                }
                Role toChange = g.getRoleById(voiceRoleId);
                if (toChange == null) {
                    log.debug("There is no role in guild {} by id {}. Skipping role update.", g, voiceRoleId);
                    return;
                }
                for (GuildVoiceState vs : g.getVoiceStates()) {
                    if (vs.getMember().getUser().isBot()) {
                        continue;
                    }
                    if (vs.inAudioChannel()) {
                        g.addRoleToMember(vs.getMember(), toChange)
                                .reason("This member was in a voice channel on bot startup, and was granted the assigned voice role.")
                                .queue();
                    }
                    else {
                        g.removeRoleFromMember(vs.getMember(), toChange)
                                .reason("This member is not in a voice channel on bot startup, and was revoked the assigned voice role.")
                                .queue();
                    }
                }
                i.getAndIncrement();
            }
        });
        log.info("Updated voice roles for {} guild(s).", i);
    }
}
