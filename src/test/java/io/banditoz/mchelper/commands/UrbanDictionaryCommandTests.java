package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@InjectTest
@Tag("external-integration")
class UrbanDictionaryCommandTests extends BaseCommandTest {
    @Inject
    UrbanDictionaryCommand udc;

    @Test
    void testUrbanDictionaryCommand() {
        setArgs("tarkov");
        assertThatCode(() -> udc.onCommand(ce)).doesNotThrowAnyException();
    }
}
