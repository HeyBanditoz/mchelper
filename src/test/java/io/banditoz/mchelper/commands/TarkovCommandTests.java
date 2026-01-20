package io.banditoz.mchelper.commands;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.Ignore;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@InjectTest
@Tag("external-integration")
@Ignore
class TarkovCommandTests extends BaseCommandTest {
    @Inject
    TarkovCommand tc;

    @Test
    void testTarkovCommand() throws Exception {
        setArgs("medical tools");
        tc.onCommand(ce);
        assertThat(embedsCaptor.getValue().get(0).getTitle()).isEqualTo("Medical tools");
    }

    @Test
    void testTarkovCommandBogusItem() throws Exception {
        // take that fuzzy matching!
        setArgs("*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        tc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("No matches found.");
    }
}
