package io.banditoz.mchelper.commands.logic.slash;

import java.time.LocalDateTime;

import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;

public class LoggableSlashCommandEvent implements Stat {
    /** The originating SlashCommandEvent. */
    private final SlashCommandEvent slashCommandEvent;
    /** Execution time of this SlashCommandEvent. */
    private final int executionTime;
    /** What the command success was. */
    private final Status status;
    /** How this command came in. */
    private final Kind kind;
    /** When this command was executed. */
    private final LocalDateTime executedWhen;
    /** Command args. */
    private final String args;

    public LoggableSlashCommandEvent(SlashCommandEvent slashCommandEvent, int executionTime, Status status, Kind kind, LocalDateTime executedWhen) {
        this.slashCommandEvent = slashCommandEvent;
        this.executionTime = executionTime;
        this.status = status;
        this.kind = kind;
        this.executedWhen = executedWhen;
        this.args = slashCommandEvent.getParamString();
    }

    @Override
    public User getUser() {
        return slashCommandEvent.getEvent().getUser();
    }

    @Override
    public Channel getChannel() {
        return slashCommandEvent.getEvent().getChannel();
    }

    @Override
    public String getClassName() {
        return slashCommandEvent.getCommand().getCommand().getClass().getSimpleName();
    }

    @Override
    public String getArgs() {
        return args;
    }

    @Override
    public int getExecutionTime() {
        return executionTime;
    }

    @Override
    public LocalDateTime getExecutedWhen() {
        return executedWhen;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Kind getKind() {
        return kind;
    }
}
