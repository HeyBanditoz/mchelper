package io.banditoz.mchelper.commands.logic.slash;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.avaje.config.Config;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.utils.FileUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlashCommandEvent implements ICommandEvent {
    private static final Logger log = LoggerFactory.getLogger(SlashCommandEvent.class);
    private final SlashCommandInteractionEvent event;
    private final SlashCommand command;
    private final String paramString;

    /** Handles defering the reply if command execution takes too long, as we only get three seconds to reply. */
    private final Future<?> defermentFuture;

    public SlashCommandEvent(@NotNull SlashCommandInteractionEvent event,
                             SlashCommand command,
                             ScheduledExecutorService ses,
                             Object[] params) {
        this.event = event;
        this.command = command;
        this.paramString = '[' + Arrays.stream(params)
                .map(o -> o instanceof String ? "\"" + o + "\"" : Objects.toString(o))
                .collect(Collectors.joining(", ")) + ']';
        this.defermentFuture = ses.schedule(() -> {
            if (!event.isAcknowledged()) {
                event.deferReply().queue();
            }
        }, Config.getInt("mchelper.slash-commands.deferment-wait-millis", 2000), TimeUnit.MILLISECONDS);
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    public SlashCommand getCommand() {
        return command;
    }

    public String getParamString() {
        return paramString;
    }

    @Override
    public User getUser() {
        return event.getUser();
    }

    @Nullable
    public Guild getGuild() {
        return event.getGuild();
    }

    @Override
    public void sendReply(String msg) {
        defermentFuture.cancel(false);
        MessageCreateData message = new MessageCreateBuilder()
                .setContent(msg)
                .setAllowedMentions(Collections.emptyList())
                .build();
        (event.isAcknowledged() ? event.getHook().sendMessage(message) : event.reply(message)).queue();
    }

    @Override
    public void sendReply(MessageCreateData msg) {
        defermentFuture.cancel(false);
        (event.isAcknowledged() ? event.getHook().sendMessage(msg) : event.reply(msg)).queue();
    }

    @Override
    public void sendImageReply(String msg, ByteArrayOutputStream data) {
        FileUpload image = FileUtils.compressPNG(data);
        defermentFuture.cancel(false);
        MessageCreateData message = new MessageCreateBuilder()
                .setContent(msg)
                .setAllowedMentions(Collections.emptyList())
                .addFiles(image)
                .build();
        (event.isAcknowledged() ? event.getHook().sendMessage(message) : event.reply(message)).queue();
        if (!new File(image.getName()).delete()) {
            log.warn("File {} was not deleted!", image.getName());
        }
    }

    @Override
    public void sendEmbedsReply(MessageEmbed... embeds) {
        defermentFuture.cancel(false);
        MessageCreateData message = new MessageCreateBuilder()
                .setEmbeds(embeds)
                .setAllowedMentions(Collections.emptyList())
                .build();
        (event.isAcknowledged() ? event.getHook().sendMessage(message) : event.reply(message)).queue();
    }
}
