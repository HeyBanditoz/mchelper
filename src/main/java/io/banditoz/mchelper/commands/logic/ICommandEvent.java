package io.banditoz.mchelper.commands.logic;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public interface ICommandEvent {
    /**
     * Sends a reply. All mentions will be sanitized.
     *
     * @param msg The reply.
     */
    void sendReply(String msg);

    /**
     * Sends a message. No transformations are applied to the message; it is sent as-is.
     *
     * @param msg The message.
     */
    void sendReply(MessageCreateData msg);

    /**
     * Sends a reply with a single attachment. All mentions will be sanitized.
     *
     * @param msg The reply.
     * @param data The image.
     */
    void sendImageReply(String msg, ByteArrayOutputStream data);

    /**
     * Sends a reply with one {@link MessageEmbed}.
     *
     * @param embed The embed to send.
     */
    default void sendEmbedReply(MessageEmbed embed) {
        sendEmbedsReply(embed);
    }

    /**
     * Sends a reply with one or many {@link MessageEmbed MessageEmbeds}.
     *
     * @param embeds The embeds to send.
     */
    void sendEmbedsReply(MessageEmbed... embeds);

    /**
     * @return The {@link User} that executed this command.
     */
    User getUser();

    /**
     * @return The {@link Guild} where the command came from. Null, if it didn't actually come from a guild.
     */
    @Nullable
    Guild getGuild();

    /**
     * @return If the command came from a guild or not.
     */
    default boolean isFromGuild() {
        return getGuild() != null;
    }
}
