package io.banditoz.mchelper.commands.logic.slash;

import java.util.ArrayList;
import java.util.List;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.banditoz.mchelper.commands.logic.Command;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for building a list of slash commands.
 * Regular commands don't follow this pattern, as they're already singletons. Slash commands have reflection that needs
 * to be handled at runtime.
 */
@Factory
public class SlashCommandFactory {
    public List<Command> commands;
    private static final Logger log = LoggerFactory.getLogger(SlashCommandFactory.class);

    @Inject
    public SlashCommandFactory(List<Command> commands) {
        this.commands = commands;
    }

    @Bean
    public List<SlashCommand> slashCommands() {
        List<SlashCommand> slashCommands = new ArrayList<>();
        for (Command command : commands) {
            try {
                slashCommands.add(new SlashCommand(command));
            } catch (MissingMethodException ignored) {
                // not converted yet/no slash command
            } catch (Exception ex) {
                log.error("Exception generating slash command for {}", command.commandName(), ex);
            }
        }
        return slashCommands;
    }
}
