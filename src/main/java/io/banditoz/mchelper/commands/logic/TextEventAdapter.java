package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextEventAdapter implements UserEventAdapter {
    private final MessageReceivedEvent event;

    public TextEventAdapter(MessageReceivedEvent event) {
        this.event = event;
    }

    @NotNull
    @Override
    public User getUser() {
        return event.getAuthor();
    }

    @Nullable
    @Override
    public Member getMember() {
        return event.getMember();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return event.isFromGuild() ? event.getGuild() : null;
    }
}
