package io.banditoz.mchelper.commands.logic.slash;

import io.banditoz.mchelper.commands.logic.UserEventAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlashEventAdapter implements UserEventAdapter {
    private final SlashCommandInteractionEvent slashEvent;

    public SlashEventAdapter(SlashCommandInteractionEvent slashEvent) {
        this.slashEvent = slashEvent;
    }

    @NotNull
    @Override
    public User getUser() {
        return slashEvent.getUser();
    }

    @Nullable
    @Override
    public Member getMember() {
        return slashEvent.getMember();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return slashEvent.getGuild();
    }
}
