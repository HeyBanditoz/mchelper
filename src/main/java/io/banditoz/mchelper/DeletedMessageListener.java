package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DeletedMessageListener extends ListenerAdapter {
    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (MCHelper.getMessageCache().getMessage(event.getMessageId()) != null) {
            Message m = MCHelper.getMessageCache().getMessage(event.getMessageId());
            StringBuilder reply = new StringBuilder("<DELETED MESSAGE from ")
                    .append(m.getAuthor().getName())
                    .append("#")
                    .append(m.getAuthor().getDiscriminator())
                    .append("> ")
                    .append(m.getContentDisplay());
            for (Message.Attachment a : m.getAttachments()) {
                reply.append("\n").append(a.getUrl());
            }
            CommandUtils.sendReply(reply.toString(), m.getTextChannel());
        }
    }
}
