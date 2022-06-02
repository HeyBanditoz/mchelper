package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TarkovCommandTests extends BaseCommandTest {
    private final TarkovCommand tc = new TarkovCommand();

    @Test
    public void testTarkovCommand() throws Exception {
        setArgs("medical tools");
        tc.onCommand(ce);
        assertThat(embedsCaptor.getValue().get(0).getTitle()).isEqualTo("Medical tools");
    }

    @Test
    public void testTarkovCommandBogusItem() throws Exception {
        // take that fuzzy matching!
        setArgs("*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        tc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("No matches found.");
    }
}
