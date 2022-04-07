package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.database.GuildConfig;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDao;
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

public class GuildVoiceListener extends ListenerAdapter {
    private final MCHelper mcHelper;
    private final GuildConfigDao dao;
    private final Logger LOGGER = LoggerFactory.getLogger(GuildVoiceListener.class);

    public GuildVoiceListener(MCHelper mcHelper) {
        this.mcHelper = mcHelper;
        this.dao = new GuildConfigDaoImpl(mcHelper.getDatabase());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        Guild g = event.getGuild();
        GuildConfig conf = dao.getConfig(g);
        if (conf == null) {
            return;
        }
        long roleId = conf.getVoiceRoleId();
        Role toChange = g.getRoleById(dao.getConfig(g).getVoiceRoleId());
        if (toChange == null) {
            LOGGER.debug("There is no role in guild {} by id {}. Skipping role update.", g, roleId);
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
        int i = 0;
        for (GuildConfig config : dao.getAllGuildConfigs()) {
            if (config.getVoiceRoleId() != 0) {
                for (Guild g : mcHelper.getJDA().getGuilds()) {
                    if (!g.getMemberById(mcHelper.getJDA().getSelfUser().getIdLong()).hasPermission(Permission.MANAGE_ROLES)) {
                        continue;
                    }
                    long roleId = dao.getConfig(g).getVoiceRoleId();
                    Role toChange = g.getRoleById(roleId);
                    if (toChange == null) {
                        LOGGER.debug("There is no role in guild {} by id {}. Skipping role update.", g, roleId);
                        continue;
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
                    i++;
                }
            }
        }
        LOGGER.info("Updated voice roles for {} guild(s).", i);
    }
}
