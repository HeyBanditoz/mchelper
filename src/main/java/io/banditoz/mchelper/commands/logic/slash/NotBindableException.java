package io.banditoz.mchelper.commands.logic.slash;

import io.banditoz.mchelper.commands.logic.BoundCommand;

/**
 * Thrown when a command cannot be exposed as a slash command because it is not a {@link BoundCommand}, and
 * so has no parameter declaration to build a Discord definition from.
 * <p>
 * Not an error. {@link SlashCommandFactory} uses it to skip commands that have not been converted yet.
 */
public class NotBindableException extends Exception {
}
