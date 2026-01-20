package io.banditoz.mchelper.commands.logic.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Utility class to map Discord parameters to Java ones, and vice versa.<br>
 * This may be rewritten in the future with an interface in mind, to handle mapping of the different types.<br>
 */
public class SlashCommandUtils {
    /**
     * Converts a Discord slash command parameter to a Java object, for use in passing into a
     * {@link io.banditoz.mchelper.commands.logic.Command}'s <code>onSlashCommand</code> method.
     *
     * @param optionMapping The parameter to convert, coming from a user invoking a slash command from Discord.
     * @return A Java object representing the mapping.
     * @throws IllegalArgumentException If such a mapping does not exist.
     */
    public static Object getValueFromOption(OptionMapping optionMapping) {
        if (optionMapping == null) {
            return null;
        }
        switch (optionMapping.getType()) {
            case STRING -> {
                return optionMapping.getAsString();
            }
            case INTEGER -> {
                return optionMapping.getAsInt();
            }
            case NUMBER -> {
                return optionMapping.getAsDouble();
            }
            case MENTIONABLE -> {
                return optionMapping.getAsMentionable();
            }
            case USER -> {
                return optionMapping.getAsUser();
            }
        }
        throw new IllegalArgumentException("Unable to convert " + optionMapping + " to Java object.");
    }

    /**
     * Converts a Java parameter argument class to an {@link OptionType}, for use in a Discord slash command definition.
     *
     * @param clazz Java class to convert, coming from the method parameter definition.
     * @return {@link OptionType} enum describing the class.
     * @throws IllegalArgumentException If such a mapping does not exist.
     */
    public static OptionType getOptionTypeFromClass(Class<?> clazz) {
        if (clazz.equals(String.class)) {
            return OptionType.STRING;
        }
        else if (clazz.equals(Integer.class)) {
            return OptionType.INTEGER;
        }
        else if (clazz.equals(User.class)) {
            return OptionType.USER;
        }
        else if (clazz.equals(double.class)) {
            return OptionType.NUMBER;
        }
        else if (clazz.equals(int.class)) {
            return OptionType.INTEGER;
        }
        else if (clazz.equals(Member.class)) {
            throw new IllegalArgumentException("Member is currently unsupported. Use User for now, and fetch the Member via Guild#getMemberById, if absolutely needed.");
        }
        throw new IllegalArgumentException("Cannot accept input of " + clazz);
    }
}
