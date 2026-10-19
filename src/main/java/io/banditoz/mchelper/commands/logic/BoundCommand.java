package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.commands.logic.params.*;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.entities.User;

/**
 * A {@link Command} that declares its parameters once, and is run the same way whether it was invoked as text or as a
 * slash command.
 * <pre>
 * &#064;Singleton
 * public class EchoCommand extends BoundCommand {
 *     public String commandName() {
 *         return "echo";
 *     }
 *
 *     protected String description() {
 *         return "Says it back.";
 *     }
 *
 *     protected Handler declare() {
 *         return Params.of().rest("message", "What to say.", this::echo);
 *     }
 *
 *     private Status echo(ICommandEvent ce, String message) {
 *         ce.sendReply(message);
 *         return Status.SUCCESS;
 *     }
 * }
 * </pre>
 * @see Params
 */
public abstract class BoundCommand extends Command {
    private volatile Handler handler;
    private volatile Help help;

    /**
     * Declares this command's parameters and handler.
     *
     * @return The result of a {@link Params} chain.
     */
    protected abstract Handler declare();

    /** @return What this command does, for help and for the slash command definition. */
    protected abstract String description();

    /** @return Whether only bot maintainers may run this. */
    protected boolean isElevated() {
        return false;
    }

    /** Enforces {@link #isElevated()}, the same way {@link ElevatedCommand} does for legacy commands. */
    @Override
    public boolean canExecute(User user) {
        return !isElevated() || CommandPermissions.isBotOwner(user);
    }

    /** @return This command's parameters and handler. */
    public final Handler handler() {
        Handler existing = handler;
        if (existing == null) {
            existing = declare();
            handler = existing;
        }
        return existing;
    }

    /** @return This command's help definition. */
    @Override
    public Help getHelp() {
        Help existing = help;
        if (existing == null) {
            existing = new Help(commandName(), isElevated())
                    .withDescription(description())
                    .withParameters(paramSpec().toUsage());
            help = existing;
        }
        return existing;
    }

    /** @return This command's parameters. */
    public final ParamSpec paramSpec() {
        return handler().params();
    }

    @Override
    public final Status onCommand(CommandEvent ce) throws Exception {
        Handler h = handler();
        Object[] values;
        try {
            values = TextBinder.bind(h.params(), ce.getRawCommandArgsString(), BindContext.of(ce));
        } catch (BindException ex) {
            ce.sendReply(ex.render(commandName(), h.params()));
            return Status.BINDING_FAIL;
        }
        return h.invoker().invoke(ce, values);
    }

}
