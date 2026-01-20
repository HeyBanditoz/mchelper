package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import net.dv8tion.jda.api.entities.channel.Channel;

/**
 * Represents a {@link CommandEvent} <i>after</i> the command has finished running. It contains how long a command
 * took to run and its return status.
 */
public class LoggableCommandEvent extends CommandEvent implements Stat {
    /** Execution time of this CommandEvent. */
    private final int executionTime;
    /** What the command success was. */
    private final Status status;
    /** How this command came in. */
    private final Kind kind;

    public LoggableCommandEvent(CommandEvent ce, int executionTime, Status status, Kind kind) {
        // TODO this really should be another class
        super(ce.getEvent(), ce.getLogger(), ce.getCommandName(), null, null, null);
        this.executionTime = executionTime;
        this.status = status;
        this.kind = kind;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Channel getChannel() {
        return getEvent().getChannel();
    }

    @Override
    public String getClassName() {
        return getCommandName();
    }

    @Override
    public String getArgs() {
        return getCommandArgsString();
    }

    @Override
    public int getExecutionTime() {
        return executionTime;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getLogMessage() {
        return Stat.super.getLogMessage();
    }
}
