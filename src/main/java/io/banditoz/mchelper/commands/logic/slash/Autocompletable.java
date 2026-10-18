package io.banditoz.mchelper.commands.logic.slash;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

/**
 * Implemented by a {@link io.banditoz.mchelper.commands.logic.Command Command} that suggests values for one or more of
 * its slash parameters as the user types. Mark the parameters themselves with {@link Param#autoComplete()}, otherwise
 * Discord will never ask.
 * <p>
 * Unlike command execution, there is no way to defer an autocomplete reply, so Discord drops the suggestions entirely
 * if they take longer than three seconds. Implementations run on their own small executor rather than the shared
 * command pool, but they should still be cheap, and must not call out to a slow external service.
 */
public interface Autocompletable {
    /**
     * Suggests values for whichever parameter the user is currently typing, available from {@link
     * CommandAutoCompleteInteractionEvent#getFocusedOption()}.
     *
     * @param event The autocomplete interaction.
     * @return Up to {@link net.dv8tion.jda.api.interactions.commands.OptionData#MAX_CHOICES 25} choices. Anything past
     * that is dropped, as Discord rejects the reply outright otherwise. Empty means no suggestions, which still lets
     * the user submit whatever they typed.
     */
    List<Choice> onAutoComplete(CommandAutoCompleteInteractionEvent event);
}
