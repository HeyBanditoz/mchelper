package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;

/**
 * Represents a {@link RegexCommandEvent} <i>after</i> the command has finished running. It contains how long a command
 * took to run and its return status.
 */
public class LoggableRegexCommandEvent extends RegexCommandEvent implements Stat {
    /** Execution time of this RegexCommandEvent. */
    private final int executionTime;
    /** What the command success was. */
    private final Status status;

    public LoggableRegexCommandEvent(RegexCommandEvent rce, int executionTime, Status status) {
        super(rce.getEvent(), rce.getMCHelper(), rce.getArgs(), rce.getLogger(), rce.getClassName());
        this.executionTime = executionTime;
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public int getExecutionTime() {
        return executionTime;
    }
}
