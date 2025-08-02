//package io.banditoz.mchelper.commands.logic;
//
//import io.banditoz.mchelper.commands.BaseCommandTest;
//import io.banditoz.mchelper.regexable.RegexableHandler;
//import io.banditoz.mchelper.database.Database;
//import org.mockito.Mockito;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.util.StringJoiner;
//
//import static org.assertj.core.api.Assertions.assertThatCode;
//import static org.assertj.core.api.Assertions.fail;
//
//public class CommandTests extends BaseCommandTest {
//    private CommandHandler ch;
//
//    public CommandTests() {
//        Mockito.when(mcHelper.getDatabase()).thenReturn(Mockito.mock(Database.class));
//    }
//
//    @Test
//    @BeforeClass
//    public void testCommandInitialization() {
//        assertThatCode(() -> ch = new CommandHandler(mcHelper)).doesNotThrowAnyException();
//    }
//
//    @Test
//    public void testRegexableInitialization() {
//        assertThatCode(() -> new RegexableHandler(mcHelper)).doesNotThrowAnyException();
//    }
//
//    @Test
//    public void noCommandShallHaveNullHelp() {
//        StringJoiner nullHelps = new StringJoiner(", ");
//        int nullHelpsCounter = 0;
//        for (Command command : ch.getCommands()) {
//            if (command.getHelp() == null) {
//                nullHelps.add(command.getClass().toString());
//                nullHelpsCounter++;
//            }
//        }
//        if (nullHelpsCounter > 0) {
//            fail(nullHelpsCounter + " command(s) have null Help objects: " + nullHelps.toString());
//        }
//    }
//
//    @Test
//    public void testHelpToStringDoesNotException() {
//        for (Command command : ch.getCommands()) {
//            command.getHelp().toString();
//        }
//    }
//}
