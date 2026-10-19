package io.banditoz.mchelper.commands.logic.params;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Converts user input into a Java value. One implementation per supported parameter type.
 * <p>
 * {@link #parseText} serves text commands and {@link #fromOption} serves slash commands, so a parameter declared once
 * binds identically from either.
 *
 * @param <T> The Java type this produces.
 */
public interface ParamType<T> {
    /** Short identifier, used in error messages. */
    String id();

    /** The Java type this binds to. */
    Class<T> javaType();

    /**
     * Parses a single token from a text command.
     *
     * @param token The raw token, as typed.
     * @param ctx   Resolution context.
     * @return The parsed value.
     * @throws BindException If the token cannot be parsed. The parameter name is filled in by the binder.
     */
    T parseText(String token, BindContext ctx) throws BindException;

    /** The Discord option type used when registering this parameter as a slash command option. */
    OptionType optionType();

    /**
     * Converts a Discord slash command option into a value.
     *
     * @param option The option Discord sent. Never null.
     * @param ctx    Resolution context.
     * @return The converted value.
     * @throws BindException If the option cannot be converted.
     */
    T fromOption(OptionMapping option, BindContext ctx) throws BindException;
}
