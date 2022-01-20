package io.banditoz.mchelper.commands;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        when(ce.getCommandArgsString()).thenReturn("\"test\" quote");
        when(ce.getCommandArgs()).thenReturn(new String[]{"\"test\"", "quote"});
        ac.onCommand(ce);
        assertThat(messageCaptor.getValue().getContentRaw()).isEqualTo("Quote added.");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuote() throws Exception {
        when(ce.getCommandArgsWithoutName()).thenReturn(new String[]{""});
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuoteWithQuoteSearch() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("test");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote"})
    public void testQuoteWithAuthorSearch() throws Exception {
        when(ce.getCommandArgsString()).thenReturn("quote");
        qc.onCommand(ce);
        MessageEmbed me = embedsCaptor.getValue().get(0);
        assertThat(me.getDescription()).contains("test", "quote");
    }

    @Test(dependsOnMethods = {"testAddquote", "testQuote", "testQuoteWithQuoteSearch", "testQuoteWithAuthorSearch"})
    public void testDeleteQuote() throws Exception {
        when(ce.getCommandArgs()).thenReturn(new String[]{"!delquote", "1"});
        dqc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Quote successfully deleted.");
    }

}