package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;

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
        super(ce.getEvent(), ce.getLogger(), ce.getMCHelper(), ce.getCommandName());
        this.executionTime = executionTime;
        this.status = status;
        this.kind = kind;
    }

    @Override
    public Status getStatus() {
        return status;
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
}
