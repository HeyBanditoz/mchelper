package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.database.NamedQuote;
import io.banditoz.mchelper.database.dao.QuotesDao;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class QuoteCommandTests extends BaseCommandTest {
    @Inject
    AddquoteCommand ac;
    @Inject
    QuoteCommand qc;
    @Inject
    DeleteQuoteCommand dqc;
    @Inject
    QuotesDao dao;

    @BeforeEach
    void clear() {
        truncate("quotes", "quote_flags");
        resetSequence("quotes_id_seq");
    }

    @Test
    void testAddquote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Quote added.");
        assertThat(dao.getQuotesByMatch("quote", ce.getGuild()).stream().map(nq -> nq.getFlags().stream().toList()).flatMap(Collection::stream).toList())
                .containsExactlyInAnyOrder(NamedQuote.Flag.DERANK, NamedQuote.Flag.EXCLUDE_QOTD);
    }

    @Test
    void testFetchNonExistentQuote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("1984");
        qc.onCommand(ce);
        assertThat(embedCaptor.getValue().getDescription()).contains("No quote found.");
    }

    @Test
    void testQuote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);

        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test
    void testQuoteWithExactQuoteSearch() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("test");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test
    void testQuoteWithQuoteSearch() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        // just for sanity
        setArgs("-e test");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test
    void testQuoteWithAuthorSearch() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("quote");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test
    void testDeleteQuote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        assertThat(dao.getQuotesByMatch("quote", ce.getGuild())).hasSize(1);

        setArgs("1");
        dqc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Quote successfully deleted.");
        assertThat(dao.getQuotesByMatch("quote", ce.getGuild())).isEmpty();
    }

    @Test
    void testDeleteNonExistentQuote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("100");
        dqc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Quote was not deleted.");
    }

    @Test
    void testAddMany() throws Exception {
        setArgs("\"123\" 123");
        ac.onCommand(ce);
        resetMocks();
        setArgs("\"456\" 456");
        ac.onCommand(ce);
    }

    @Test
    void testFetchMany() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("\"testing again\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("-a");
        qc.onCommand(ce);

        List<MessageEmbed> embeds = embedsCaptor.getValue();
        assertThat(embeds.get(0).getDescription()).contains("1 of 2");
        assertThat(embeds.size()).isEqualTo(2);
    }

    @Test
    void testQuoteStats() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("-s");
        qc.onCommand(ce);
        assertThat(embedCaptor.getValue().getDescription()).contains("""
                ```
                Rank  Name
                1.    NFoo                 1
                _____________________________
                Total                      1
                ```""");

    }

    @Test
    void testAddquoteGoodValues_inAuthor() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("\"test\" crap");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Quote added.");
        assertThat(dao.getQuotesByMatch("crap", ce.getGuild()).stream().map(nq -> nq.getFlags().stream().toList()).flatMap(Collection::stream).toList())
                .isEmpty();
    }

    @Test
    void testAddquoteGoodValues_inQuote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        resetMocks();

        setArgs("\"Bad\" test");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Quote added.");
        assertThat(dao.getQuotesByMatch("crap", ce.getGuild()).stream().map(nq -> nq.getFlags().stream().toList()).flatMap(Collection::stream).toList())
                .isEmpty();
    }
}
