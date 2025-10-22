package io.banditoz.mchelper;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Did the DI container start successfully? */
@InjectTest
class SmokeTest extends BaseTest {
    @Inject
    JDA jda;

    @Test
    void test() {
        // JDA should be in the container.
        assertThat(jda).isNotNull();
        // Factory-initialized beans should be in the container.
        assertThat(jda.retrieveApplicationInfo().complete()).isNotNull();
    }
}
