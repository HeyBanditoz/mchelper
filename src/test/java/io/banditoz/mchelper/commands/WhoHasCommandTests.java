package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@InjectTest
class WhoHasCommandTests extends BaseCommandTest {
    @Inject
    WhoHasCommand whc;

    @Test
    void testWhoHasCommand() throws Exception {
        setArgs("732863308738330636");
        whc.onCommand(ce);
        assertThat(embedCaptor.getValue().getDescription()).isEqualTo("Members that have role *Role:*\n<@!404837963697225729>, <@!163094867910590464>");
    }

    @Test
    void testWhoHasCommandInvalidRole() {
        setArgs("0");
        assertThatThrownBy(() -> whc.onCommand(ce));
    }
}
