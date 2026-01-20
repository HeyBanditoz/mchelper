package io.banditoz.mchelper.commands.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

// TODO, evaluate the need of this interface. Does UserEvent work instead?
public interface UserEventAdapter {
    @Nonnull
    User getUser();

    @Nullable
    Member getMember();

    @Nullable
    default Member getAuthor() {
        return getMember();
    }

    @Nullable
    Guild getGuild();
}
