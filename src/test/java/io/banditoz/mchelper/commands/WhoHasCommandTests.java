package io.banditoz.mchelper.commands;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;

public class WhoHasCommandTests extends BaseCommandTest {
    private final WhoHasCommand whc;

    public WhoHasCommandTests() {
        this.whc = spy(new WhoHasCommand());
    }

    @Test
    public void testWhoHasCommand() throws Exception {
        setArgs("732863308738330636");
        whc.onCommand(ce);
        assertThat(embedCaptor.getValue().getDescription()).isEqualTo("Members that have role *Role:*\n<@!404837963697225729>, <@!163094867910590464>");
    }

    @Test
    public void testWhoHasCommandInvalidRole() {
        setArgs("0");
        assertThatThrownBy(() -> whc.onCommand(ce));
    }
}
