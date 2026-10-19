package io.banditoz.mchelper.commands.logic.params;

import javax.annotation.Nullable;

import io.banditoz.mchelper.commands.logic.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public record BindContext(JDA jda, @Nullable Guild guild, User invoker) {
    public static BindContext of(CommandEvent ce) {
        return new BindContext(ce.getEvent().getJDA(), ce.getGuild(), ce.getUser());
    }
}
