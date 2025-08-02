package io.banditoz.mchelper.commands;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.banditoz.mchelper.database.NamedQuote;
import io.banditoz.mchelper.database.dao.QuotesDao;
import io.banditoz.mchelper.database.dao.QuotesDaoImpl;
import io.banditoz.mchelper.interactions.InteractionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.testng.annotations.Test;

@Test(dependsOnGroups = {"DatabaseInitializationTests"})
public class QuoteCommandTests extends BaseCommandTest {
    private final AddquoteCommand ac;
    private final QuoteCommand qc;
    private final DeleteQuoteCommand dqc;
    private final QuotesDao dao;

    public QuoteCommandTests() {
        this.dao = new QuotesDaoImpl(DB);
        this.ac = new AddquoteCommand(dao, mock(InteractionListener.class));
        this.qc = new QuoteCommand(dao, mock(JDA.class));
        this.dqc = new DeleteQuoteCommand(dao);
    }

    public void testAddquote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Quote added.");
        assertThat(dao.getQuotesByMatch("quote", ce.getGuild()).stream().map(nq -> nq.getFlags().stream().toList()).flatMap(Collection::stream).toList())
                .containsExactlyInAnyOrder(NamedQuote.Flag.DERANK, NamedQuote.Flag.EXCLUDE_QOTD);
    }

    @Test(timeOut = 5000L) // 5 second timeout
    public void testFetchNonExistentQuote() throws Exception {
        setArgs("1984");
        qc.onCommand(ce);
        assertThat(embedCaptor.getValue().getDescription()).contains("No quote found.");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuote() throws Exception {
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuoteWithExactQuoteSearch() throws Exception {
        setArgs("test");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuoteWithQuoteSearch() throws Exception {
        // just for sanity
        setArgs("-e test");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuoteWithAuthorSearch() throws Exception {
        setArgs("quote");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote", "testQuote", "testQuoteWithQuoteSearch", "testQuoteWithAuthorSearch"})
    public void testDeleteQuote() throws Exception {
        assertThat(dao.getQuotesByMatch("quote", ce.getGuild())).hasSize(1);
        setArgs("1");
        dqc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Quote successfully deleted.");
        assertThat(dao.getQuotesByMatch("quote", ce.getGuild())).isEmpty();
    }

    @Test(dependsOnMethods = {"testAddquote", "testQuote", "testQuoteWithQuoteSearch", "testQuoteWithAuthorSearch"})
    public void testDeleteNonExistentQuote() throws Exception {
        setArgs("100");
        dqc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Quote was not deleted.");
    }

    @Test(dependsOnMethods = {"testDeleteQuote"})
    public void testAddMany() throws Exception {
        setArgs("\"123\" 123");
        ac.onCommand(ce);
        clearArgs();
        setArgs("\"456\" 456");
        ac.onCommand(ce);
    }

    @Test(dependsOnMethods = {"testAddMany"})
    public void testFetchMany() throws Exception {
        setArgs("-a");
        qc.onCommand(ce);

        List<MessageEmbed> embeds = embedsCaptor.getValue();
        assertThat(embeds.get(0).getDescription()).contains("1 of 2");
        assertThat(embeds.size()).isEqualTo(2);
    }

    @Test(dependsOnMethods = "testAddMany")
    public void testQuoteStats() throws Exception {
        setArgs("-s");
        qc.onCommand(ce);
        assertThat(embedCaptor.getValue().getDescription()).contains("""
                ```
                Rank  Name
                1.    NFoo                 2
                _____________________________
                Total                      2
                ```""");

    }

    @Test(dependsOnMethods = "testQuoteStats")
    public void testAddquoteGoodValues_inAuthor() throws Exception {
        setArgs("\"test\" crap");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Quote added.");
        assertThat(dao.getQuotesByMatch("crap", ce.getGuild()).stream().map(nq -> nq.getFlags().stream().toList()).flatMap(Collection::stream).toList())
                .isEmpty();
    }

    @Test(dependsOnMethods = "testQuoteStats")
    public void testAddquoteGoodValues_inQuote() throws Exception {
        setArgs("\"Bad\" test");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Quote added.");
        assertThat(dao.getQuotesByMatch("crap", ce.getGuild()).stream().map(nq -> nq.getFlags().stream().toList()).flatMap(Collection::stream).toList())
                .isEmpty();
    }
}
