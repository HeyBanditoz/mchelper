package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@InjectTest
class NowCommandTests extends BaseCommandTest {
    @Inject
    NowCommand nc;

    @Test
    void testNowCommand() throws Exception {
        nc.onCommand(ce);
        String result = stringCaptor.getValue();
        assertThat(result).startsWith("<t:").endsWith(">");
        long unixTime = Long.parseLong(result.substring(3, 13)) * 1000;
        assertThat(unixTime).isCloseTo(System.currentTimeMillis(), within(10000L));
    }
}
