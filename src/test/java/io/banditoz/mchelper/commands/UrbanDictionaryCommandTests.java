package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.spy;

public class UrbanDictionaryCommandTests extends BaseCommandTest {
    private final UrbanDictionaryCommand udc = spy(UrbanDictionaryCommand.class);

    @Test
    public void testUrbanDictionaryCommand() throws Exception {
        setArgs("tarkov");
        assertThatCode(() -> udc.onCommand(ce)).doesNotThrowAnyException();
    }
}
