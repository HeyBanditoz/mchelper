package io.banditoz.mchelper.commands;

import static org.assertj.core.api.Assertions.assertThat;

import io.banditoz.mchelper.Http;
import io.banditoz.mchelper.ObjectMapperFactory;
import io.banditoz.mchelper.tarkovmarket.TarkovMarketSearcher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TarkovCommandTests extends BaseCommandTest {
    private TarkovCommand tc;

    @BeforeClass
    public void tarkovCommand() {
        Http http = new Http(new ObjectMapperFactory().objectMapper());
        TarkovMarketSearcher searcher = new TarkovMarketSearcher(http.getTarkovClient());
        tc = new TarkovCommand(searcher);
    }

    @Test
    public void testTarkovCommand() throws Exception {
        setArgs("medical tools");
        tc.onCommand(ce);
        assertThat(embedsCaptor.getValue().get(0).getTitle()).isEqualTo("Medical tools");
    }

    @Test
    public void testTarkovCommandBogusItem() throws Exception {
        // take that fuzzy matching!
        setArgs("*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        tc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("No matches found.");
    }
}
