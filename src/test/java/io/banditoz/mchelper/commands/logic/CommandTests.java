package io.banditoz.mchelper.commands.logic;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.commands.BaseCommandTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.fail;

@InjectTest
class CommandTests extends BaseCommandTest {
    @Inject
    private CommandHandler ch;

    @Test
    void noCommandShallHaveNullHelp() {
        StringJoiner nullHelps = new StringJoiner(", ");
        int nullHelpsCounter = 0;
        for (Command command : ch.getCommands()) {
            if (command.getHelp() == null) {
                nullHelps.add(command.getClass().toString());
                nullHelpsCounter++;
            }
        }
        if (nullHelpsCounter > 0) {
            fail(nullHelpsCounter + " command(s) have null Help objects: " + nullHelps.toString());
        }
    }

    @Test
    void testHelpToStringDoesNotException() {
        for (Command command : ch.getCommands()) {
            command.getHelp().toString();
        }
    }
}
