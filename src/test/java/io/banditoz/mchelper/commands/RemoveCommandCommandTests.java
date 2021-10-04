package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.CommandHandler;
import io.banditoz.mchelper.commands.logic.CommandTests;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class RemoveCommandCommandTests extends BaseCommandTest {
    private final RemoveCommandCommand rcc;

    public RemoveCommandCommandTests() throws Exception {
        MCHelper mcHelper = Mockito.mock(MCHelper.class);
        Mockito.when(mcHelper.getSettings()).thenReturn(CommandTests.getMockSettings());
        CommandHandler ch = new CommandHandler(mcHelper);
        when(mcHelper.getCommandHandler()).thenReturn(ch);
        this.rcc = spy(new RemoveCommandCommand());
        when(ce.getMCHelper()).thenReturn(mcHelper);
    }

    @Test
    public void testRemoveCommandCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!remove", "flip"});
        rcc.onCommand(ce);
        assertEquals("Command successfully removed for this runtime.", stringCaptor.getValue());
    }

    @Test
    public void testRemoveBogusCommand() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!remove", "bogus"});
        rcc.onCommand(ce);
        assertEquals("Command not found.", stringCaptor.getValue());
    }
}
