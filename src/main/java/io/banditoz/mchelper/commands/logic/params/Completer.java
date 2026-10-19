package io.banditoz.mchelper.commands.logic.params;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

@FunctionalInterface
public interface Completer {
    List<Choice> complete(CommandAutoCompleteInteractionEvent event) throws Exception;
}
