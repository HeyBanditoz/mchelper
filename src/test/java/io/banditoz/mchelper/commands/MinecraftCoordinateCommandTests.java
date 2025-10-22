package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class MinecraftCoordinateCommandTests extends BaseCommandTest {
    @Inject
    OverworldCommand oc;
    @Inject
    NetherCommand nc;
    @Inject
    EangleCommand ec;

    @Test
    void testOverworldCommand() throws Exception {
        setArgs("2384 1202");
        oc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("19072, 9616");
    }

    @Test
    void testNetherCommand() throws Exception {
        setArgs("19348 -2364");
        nc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("2418, -295");
    }

    @Test
    void testEangleCommand() throws Exception {
        setArgs("23 96 1952 -1956");
        ec.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("**Yaw:** -136.8 **Distance:** 2816.3");
    }
}
