package io.banditoz.mchelper.stats;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;

/**
 * A class which represents anything that the bot runs on behalf of a user, such as a command or a listener, to be
 * recorded by a statistics recorder.
 */
public interface Stat {
    /**
     * Returns the {@link MessageReceivedEvent} associated with this statistic.
     *
     * @return The {@link MessageReceivedEvent}.
     */
    MessageReceivedEvent getEvent();
    /**
     * Returns the name of the command, regexable, or anything that the bot could run on behalf of the user's class
     * name.
     *
     * @return The class name.
     */
    String getClassName();
    /**
     * Returns the arguments used in this statistic.
     *
     * @return The arguments
     */
    String getArgs();
    /**
     * Returns the execution time (in ms) this statistic's class took to process.
     *
     * @return The execution time (in ms.)
     */
    int getExecutionTime();
    /**
     * Returns when this was executed.
     *
     * @return The {@link LocalDateTime} when this was executed.
     */
    LocalDateTime getExecutedWhen();
    /**
     * Returns the {@link Status} associated with this statistic of the executable
     *
     * @return The status.
     */
    Status getStatus();
    /**
     * Returns a String representation of this {@link Stat}, meant for logging.
     *
     * @return A String representation.
     */
    default String getLogMessage() {
        return String.format("%s returned %s in %d ms. <%s@%s> %s",
                this.getClassName(),
                this.getStatus(),
                this.getExecutionTime(),
                this.getEvent().getAuthor().toString(),
                this.getEvent().getChannel().toString(),
                this.getArgs().isEmpty() ? "<no arguments>" : this.getArgs());
    }
}
