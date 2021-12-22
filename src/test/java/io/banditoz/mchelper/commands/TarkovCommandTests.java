package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TarkovCommandTests extends BaseCommandTest {
    private final TarkovCommand tc = new TarkovCommand();

    @Test
    public void testTarkovCommand() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("medical tools");
        tc.onCommand(ce);
        assertThat(embedsCaptor.getValue().get(0).getTitle()).isEqualTo("Medical tools");
    }

    @Test
    public void testTarkovCommandBogusItem() throws Exception {
        // take that fuzzy matching!
        when(ce.getCommandArgsString()).thenReturn("*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        tc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("No matches found.");
    }
}
