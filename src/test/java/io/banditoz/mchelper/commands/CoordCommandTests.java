package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class CoordCommandTests extends BaseCommandTest {
    @Inject
    CoordCommand cc;

    @BeforeEach
    void clear() {
        truncate("coordinates");
    }

    @Test
    void testCoordinateAdd() throws Exception {
        setArgs("add TestBase 100 -100");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("100, -100 saved.");
    }

    @Test
    void testCoordinateGetOne() throws Exception {
        setArgs("add TestBase 100 -100");
        cc.onCommand(ce);
        resetMocks();
        setArgs("show TestBase");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("100, -100");
    }

    @Test
    void testCoordinateGetAll() throws Exception {
        setArgs("add TestBase 100 -100");
        cc.onCommand(ce);
        resetMocks();
        setArgs("list");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("TestBase: 100, -100");
    }

    @Test
    void testCoordinateDelete() throws Exception {
        setArgs("add TestBase 100 -100");
        cc.onCommand(ce);
        resetMocks();
        setArgs("remove test");
        cc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Deleted.");
    }
}
