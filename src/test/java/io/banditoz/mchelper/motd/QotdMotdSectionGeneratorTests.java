//package io.banditoz.mchelper.motd;
//
//import io.banditoz.mchelper.MCHelper;
//import io.banditoz.mchelper.commands.BaseCommandTest;
//import io.banditoz.mchelper.utils.database.NamedQuote;
//import io.banditoz.mchelper.utils.database.dao.QuotesDao;
//import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
//import net.dv8tion.jda.api.entities.Guild;
//import net.dv8tion.jda.api.entities.MessageEmbed;
//import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.AfterTest;
//import org.testng.annotations.Test;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.EnumSet;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@Test(dependsOnGroups = {"DatabaseInitializationTests"})
//public class QotdMotdSectionGeneratorTests extends BaseCommandTest {
//    private final QuotesDao quotesDao;
//    private final MCHelper mcHelper = mock(MCHelper.class);
//    private final TextChannel tc = mock(TextChannel.class);
//    private final Guild g = mock(Guild.class);
//
//    public QotdMotdSectionGeneratorTests() {
//        this.quotesDao = new QuotesDaoImpl(DB);
//        when(mcHelper.getDatabase()).thenReturn(DB);
//        when(tc.getGuild()).thenReturn(g);
//    }
//
//    @AfterMethod
//    public void clearQuotesTables() throws SQLException {
//        try (Connection c = DB.getConnection()) {
//            c.prepareStatement("TRUNCATE quotes CASCADE").execute();
//        }
//    }
//
//    @AfterTest
//    public void clearQuotesTablesa() throws SQLException {
//        try (Connection c = DB.getConnection()) {
//            c.prepareStatement("TRUNCATE quotes CASCADE").execute();
//        }
//    }
//
//    @Test
//    public void testQotd_happyPath() throws SQLException {
//        NamedQuote nq = new NamedQuote();
//        nq.setQuote("hello world");
//        nq.setQuoteAuthor("the person");
//        nq.setGuildId(1);
//        when(g.getIdLong()).thenReturn(Long.valueOf(1));
//        quotesDao.saveQuote(nq, EnumSet.noneOf(NamedQuote.Flag.class));
//        QotdMotdSectionGenerator g = new QotdMotdSectionGenerator(mcHelper);
//        MessageEmbed embed = g.generate(tc);
//        assertThat(embed.getDescription()).contains("hello world");
//        assertThat(embed.getDescription()).contains("the person");
//    }
//
//    @Test
//    public void testQotd_fromAnotherGuild_notFound() throws SQLException {
//        NamedQuote nq = new NamedQuote();
//        nq.setQuote("hello world");
//        nq.setQuoteAuthor("the person");
//        nq.setGuildId(2);
//        when(g.getIdLong()).thenReturn(Long.valueOf(1));
//        quotesDao.saveQuote(nq, EnumSet.noneOf(NamedQuote.Flag.class));
//        QotdMotdSectionGenerator g = new QotdMotdSectionGenerator(mcHelper);
//        MessageEmbed embed = g.generate(tc);
//        assertThat(embed).isNull();
//    }
//
//    @Test
//    public void testQotd_deleted_notFound() throws SQLException {
//        NamedQuote nq = new NamedQuote();
//        nq.setQuote("hello world");
//        nq.setQuoteAuthor("the person");
//        nq.setGuildId(1);
//        when(g.getIdLong()).thenReturn(Long.valueOf(1));
//        quotesDao.saveQuote(nq, EnumSet.of(NamedQuote.Flag.HIDDEN));
//        QotdMotdSectionGenerator g = new QotdMotdSectionGenerator(mcHelper);
//        MessageEmbed embed = g.generate(tc);
//        assertThat(embed).isNull();
//    }
//
//    @Test
//    public void testQotd_excluded_notFound() throws SQLException {
//        NamedQuote nq = new NamedQuote();
//        nq.setQuote("hello world");
//        nq.setQuoteAuthor("the person");
//        nq.setGuildId(1);
//        when(g.getIdLong()).thenReturn(Long.valueOf(1));
//        quotesDao.saveQuote(nq, EnumSet.of(NamedQuote.Flag.EXCLUDE_QOTD));
//        QotdMotdSectionGenerator g = new QotdMotdSectionGenerator(mcHelper);
//        MessageEmbed embed = g.generate(tc);
//        assertThat(embed).isNull();
//    }
//
//    @Test
//    public void testQotdExcluded_excluded_found() throws SQLException {
//        NamedQuote nq = new NamedQuote();
//        nq.setQuote("hello world");
//        nq.setQuoteAuthor("the person");
//        nq.setGuildId(1);
//        when(g.getIdLong()).thenReturn(Long.valueOf(1));
//        quotesDao.saveQuote(nq, EnumSet.of(NamedQuote.Flag.EXCLUDE_QOTD));
//        QotdMotdExcludedSectionGenerator g = new QotdMotdExcludedSectionGenerator(mcHelper);
//        MessageEmbed embed = g.generate(tc);
//        assertThat(embed.getDescription()).contains("hello world");
//        assertThat(embed.getDescription()).contains("the person");
//    }
//
//    @Test
//    public void testQotdExcluded_excluded_fromAnotherGuild_notFound() throws SQLException {
//        NamedQuote nq = new NamedQuote();
//        nq.setQuote("hello world");
//        nq.setQuoteAuthor("the person");
//        nq.setGuildId(2);
//        when(g.getIdLong()).thenReturn(Long.valueOf(1));
//        quotesDao.saveQuote(nq, EnumSet.of(NamedQuote.Flag.EXCLUDE_QOTD));
//        QotdMotdExcludedSectionGenerator g = new QotdMotdExcludedSectionGenerator(mcHelper);
//        MessageEmbed embed = g.generate(tc);
//        assertThat(embed).isNull();
//    }
//}
