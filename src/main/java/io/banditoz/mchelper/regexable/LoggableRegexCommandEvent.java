package io.banditoz.mchelper.regexable;

import io.banditoz.mchelper.stats.Kind;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import net.dv8tion.jda.api.entities.channel.Channel;

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
        super(rce.getEvent(), rce.getArgs(), rce.getLogger(), rce.getClassName(), null);
        this.executionTime = executionTime;
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public Kind getKind() {
        return Kind.TEXT;
    }

    @Override
    public String getLogMessage() {
        return Stat.super.getLogMessage();
    }

    @Override
    public Channel getChannel() {
        return getEvent().getChannel();
    }

    public int getExecutionTime() {
        return executionTime;
    }
}
