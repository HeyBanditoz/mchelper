package io.banditoz.mchelper.stats;

import java.time.LocalDateTime;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;

/**
 * A class which represents anything that the bot <i>ran</i> on behalf of a user, such as a command or a listener, to be
 * recorded by a statistics recorder.
 */
public interface Stat {
    User getUser(); // TODO javadoc
    Channel getChannel(); // TODO javadoc
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
     * Returns how this command was executed via the {@link Kind} enum.
     *
     * @return The kind.
     */
    Kind getKind();
    /**
     * Returns a String representation of this {@link Stat}, meant for logging.
     *
     * @return A String representation.
     */
    default String getLogMessage() {
        return String.format("%s returned %s in %d ms via %s. <%s@%s> %s",
                this.getClassName(),
                this.getStatus(),
                this.getExecutionTime(),
                this.getKind(),
                this.getUser().toString(),
                this.getChannel().toString(),
                this.getArgs().isEmpty() ? "<no arguments>" : this.getArgs());
    }
}
