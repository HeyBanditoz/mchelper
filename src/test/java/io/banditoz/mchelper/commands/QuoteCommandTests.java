package io.banditoz.mchelper.commands;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Test(dependsOnGroups = {"DatabaseInitializationTests"})
public class QuoteCommandTests extends BaseCommandTest {
    private final AddquoteCommand ac;
    private final QuoteCommand qc;
    private final DeleteQuoteCommand dqc;

    public QuoteCommandTests() {
        this.ac = new AddquoteCommand();
        this.qc = new QuoteCommand();
        this.dqc = new DeleteQuoteCommand();
    }

    public void testAddquote() throws Exception {
        setArgs("\"test\" quote");
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContentRaw()).isEqualTo("Quote added.");
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
        setArgs("1");
        dqc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Quote successfully deleted.");
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
}
