package io.banditoz.mchelper;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.regexable.RegexableHandler;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.Database;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandTests {
    private final MCHelper mcHelper;

    public CommandTests() {
        mcHelper = Mockito.mock(MCHelper.class);
        Settings settings = getMockSettings();
        Mockito.when(mcHelper.getDatabase()).thenReturn(Mockito.mock(Database.class));
        Mockito.when(mcHelper.getSettings()).thenReturn(settings);
    }

    @Test
    public void testCommandInitialization() {
        assertDoesNotThrow(() -> new CommandHandler(mcHelper));
    }

    @Test
    public void testRegexableInitialization() {
        assertDoesNotThrow(() -> new RegexableHandler(mcHelper));
    }

    @Test
    public void noCommandShallHaveNullHelp() {
        AtomicReference<CommandHandler> ch = new AtomicReference<>();
        assertDoesNotThrow(() -> ch.set(new CommandHandler(mcHelper)), "Exception initializing the CommandHandler. Cannot continue test.");
        StringJoiner nullHelps = new StringJoiner(", ");
        int nullHelpsCounter = 0;
        for (Command command : ch.get().getCommands()) {
            if (command.getHelp() == null) {
                nullHelps.add(command.getClass().toString());
                nullHelpsCounter++;
            }
        }
        if (nullHelpsCounter > 0) {
            fail(nullHelpsCounter + " command(s) have null Help objects: " + nullHelps.toString());
        }
    }

    private Settings getMockSettings() {
        Settings settings = new Settings();
        List<String> defaultOwners = new ArrayList<>();
        defaultOwners.add("12341234");
        settings.setDiscordToken("asdgfstherjuhgfj");
        settings.setBotOwners(defaultOwners);
        settings.setOwlBotToken("eryhue354uh3y4ewtgs");
        settings.setCommandThreads(1);
        settings.setFinnhubKey("35uy7ewsgfhed");
        settings.setRiotApiKey("we98ytfghioned.");
        settings.setDatabaseName("no_database");
        settings.setDatabaseHostAndPort("localhost:3306");
        settings.setDatabaseUsername("root");
        settings.setDatabasePassword("toor");
        settings.setTarkovMarketApiKey("sgfjklw4epoitju");
        return settings;
    }

}
