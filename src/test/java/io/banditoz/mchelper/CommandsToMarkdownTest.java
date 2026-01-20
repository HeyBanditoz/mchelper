package io.banditoz.mchelper;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;
import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.commands.HelpCommand;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.slash.SlashCommand;
import io.banditoz.mchelper.regexable.Regexable;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * This isn't really a test, but it generates the list of commands for use in the repository.<br>
 * It's <i>written</i> as a test as DI is handling command creation for us - and since some commands inject
 * <i>dependencies</i>, we can't easily create them.
 */
@InjectTest
class CommandsToMarkdownTest {
    @Inject
    List<Command> diCommands;
    @Inject
    List<Regexable> diRegexables;
    @Inject
    List<SlashCommand> diSlashCommands;

    private static final ClassNameComparator comp = new ClassNameComparator();

    @Test
    void commandsToMarkdown() throws Exception {
        // this is a bit wacko! but HelpCommand is not a bean.
        diCommands.add(new HelpCommand(diCommands));

        // what's missing? the test config may not be setup correctly if any are
        List<? extends Class<? extends Command>> commands = diCommands.stream().map(Command::getClass).toList();
        List<? extends Class<? extends Regexable>> regexables = diRegexables.stream().map(Regexable::getClass).toList();

        Set<Class<? extends Command>> allCommands = ClassUtils.getAllSubtypesOf(Command.class)
                .stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .collect(Collectors.toSet());
        Set<Class<? extends Regexable>> allRegexables = ClassUtils.getAllSubtypesOf(Regexable.class)
                .stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .collect(Collectors.toSet());

        commands.forEach(allCommands::remove);
        regexables.forEach(allRegexables::remove);

        assertThat(allCommands)
                .as("Commands that weren't registered as a bean were found. Please ensure all commands are registered with DI, " +
                        "otherwise the COMMANDS.md file will be missing commands.")
                .isEmpty();
        assertThat(allRegexables)
                .as("Regexables that weren't registered as a bean were found. Please ensure all regexables are registered with DI, " +
                        "otherwise the COMMANDS.md file will be missing regexables.")
                .isEmpty();

        // we have all possible commands registered with DI!
        // build the markdown command list
        StringBuilder markdown = new StringBuilder("# Commands\n");

        markdown.append("There are a total of ").append(diCommands.size()).append(" commands and ")
                .append(regexables.size()).append(" regex listeners.\n\nIf a command name has _(S)_, it also has a slash command.\n\n");

        List<? extends Class<? extends Command>> slashCommandClasses = diSlashCommands
                .stream()
                .map(sc -> sc.getCommand().getClass())
                .toList();
        for (Command command : diCommands.stream().sorted(comp).toList()) {
            markdown.append("### ").append(command.getClass().getSimpleName());
            if (slashCommandClasses.contains(command.getClass())) {
                markdown.append(" _(S)_");
            }
            markdown.append("\n");
            markdown.append(command.getHelp().toString());
            markdown.append("\n\n");
        }

        markdown.append("# Regex Listeners\n");

        for (Regexable regexable : diRegexables.stream().sorted(comp).toList()) {
            markdown.append("### ").append(regexable.getClass().getSimpleName()).append('\n');
            markdown.append("`").append(escape(regexable.getPattern().toString())).append('`');
            markdown.append("\n\n");
        }
        Files.write(Path.of("COMMANDS.md"), markdown.toString().getBytes());
    }

    private static class ClassNameComparator implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
        }
    }
}
