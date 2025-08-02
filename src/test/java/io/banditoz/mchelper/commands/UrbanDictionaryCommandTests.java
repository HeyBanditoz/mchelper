package io.banditoz.mchelper.commands;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.banditoz.mchelper.Http;
import io.banditoz.mchelper.ObjectMapperFactory;
import io.banditoz.mchelper.urbandictionary.UDSearcher;
import org.testng.annotations.Test;

public class UrbanDictionaryCommandTests extends BaseCommandTest {
    private final UrbanDictionaryCommand udc;

    public UrbanDictionaryCommandTests() {
        this.udc = new UrbanDictionaryCommand(new UDSearcher(new Http(new ObjectMapperFactory().objectMapper()).getUrbanDictionaryClient()));
    }

    @Test
    public void testUrbanDictionaryCommand() throws Exception {
        setArgs("tarkov");
        assertThatCode(() -> udc.onCommand(ce)).doesNotThrowAnyException();
    }
}
