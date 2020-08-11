package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.mock_classes.MCHelperTestImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.fail;

public class CommandTests {
    private final MCHelper mcHelper = new MCHelperTestImpl();

    @Test
    public void noDuplicateCommands() {
        Set<String> newList = new HashSet<>();
        StringJoiner duplicateCommands = new StringJoiner(", ");
        int duplicates = 0;
        for (Command command : mcHelper.getCommands()) {
            if (!newList.add(command.commandName())) {
                duplicateCommands.add(command.getClass().toString());
                duplicates++;
            }
        }
        if (duplicates > 0) {
            fail(duplicates + " duplicate command(s): " + duplicateCommands.toString());
        }
    }

    @Test
    public void noCommandShallHaveNullHelp() {
        StringJoiner nullHelps = new StringJoiner(", ");
        int nullHelpsCounter = 0;
        for (Command command : mcHelper.getCommands()) {
            if (command.getHelp() == null) {
                nullHelps.add(command.getClass().toString());
                nullHelpsCounter++;
            }
        }
        if (nullHelpsCounter > 0) {
            fail(nullHelpsCounter + " command(s) have null Help objects: " + nullHelps.toString());
        }
    }
}
