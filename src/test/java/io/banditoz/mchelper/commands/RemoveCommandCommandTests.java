package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.CommandHandler;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class RemoveCommandCommandTests extends BaseCommandTest {
    private final RemoveCommandCommand rcc;

    public RemoveCommandCommandTests() throws Exception {
        CommandHandler ch = new CommandHandler(mcHelper);
        when(mcHelper.getCommandHandler()).thenReturn(ch);
        this.rcc = spy(new RemoveCommandCommand());
        when(ce.getMCHelper()).thenReturn(mcHelper);
    }

    @Test
    public void testRemoveCommandCommand() throws Exception {
        setArgs("flip");
        rcc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Command successfully removed for this runtime.");
    }

    @Test
    public void testRemoveBogusCommand() throws Exception {
        setArgs("bogus");
        rcc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Command not found.");
    }
}
