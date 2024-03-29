package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.PollService;
import io.banditoz.mchelper.runnables.PollCullerRunnable;
import io.banditoz.mchelper.utils.database.Poll;
import io.banditoz.mchelper.utils.database.PollQuestion;
import io.banditoz.mchelper.utils.database.dao.PollsDao;
import io.banditoz.mchelper.utils.database.dao.PollsDaoImpl;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Test(dependsOnGroups = {"DatabaseInitializationTests"})
public class PollCommandTests extends BaseCommandTest {
    private final PollCommand pc = new PollCommand();
    private final PollsDao dao = new PollsDaoImpl(DB);

    @BeforeClass
    public void setup() throws Exception {
        PollService ps = new PollService(mcHelper);
        when(ce.getMCHelper().getPollService()).thenReturn(ps);
    }

    // TODO expand these tests. They're kinda barebones.
    @Test
    public void testPollCommandSingle() throws Exception {
        Message m = mock(Message.class);
        when(m.getIdLong()).thenReturn(100L);
        when(ce.getEvent().getChannel().sendMessage(any(MessageCreateData.class)).complete()).thenReturn(m);

        setArgs("\"Best chicken nugget\" single Fried Raw \"Double Fried\" Baked \"Air Fried\" \"Heated with Radiation\"");
        pc.onCommand(ce);
        Poll poll = dao.getAllPolls().get(0);
        Map<PollQuestion, Integer> results = dao.getResults(poll.questions());
        assertThat(results).size().isEqualTo(6);
        assertThat(results.values()).allMatch(i -> i == 0);
        PollQuestion underTest = results.keySet().stream().toList().get(0);

        dao.toggleVote(underTest.buttonUuid(), ce.getEvent().getAuthor(), poll);

        results = dao.getResults(poll.questions());
        assertThat(results).size().isEqualTo(6);
        assertThat(results.get(underTest)).isEqualTo(1);

        dao.toggleVote(underTest.buttonUuid(), ce.getEvent().getAuthor(), poll);

        results = dao.getResults(poll.questions());
        assertThat(results).size().isEqualTo(6);
        assertThat(results.get(underTest)).isEqualTo(0);
    }

    @Test
    public void testPollCommandMultiple() throws Exception {
        Message m = mock(Message.class);
        when(m.getIdLong()).thenReturn(101L);
        when(ce.getEvent().getChannel().sendMessage(any(MessageCreateData.class)).complete()).thenReturn(m);

        setArgs("\"Best chicken nugget\" multiple Fried Raw \"Double Fried\" Baked \"Air Fried\" \"Heated with Radiation\"");
        pc.onCommand(ce);
        Poll poll = dao.getAllPolls().get(0);
        Map<PollQuestion, Integer> results = dao.getResults(poll.questions());
        assertThat(results).size().isEqualTo(6);
        assertThat(results.values()).allMatch(i -> i == 0);
        List<PollQuestion> pqs = results.keySet().stream().toList();
        PollQuestion underTest = pqs.get(0);
        PollQuestion underTest2 = pqs.get(1);

        dao.toggleVote(underTest.buttonUuid(), ce.getEvent().getAuthor(), poll);

        results = dao.getResults(poll.questions());
        assertThat(results).size().isEqualTo(6);
        assertThat(results.get(underTest)).isEqualTo(1);

        dao.toggleVote(underTest2.buttonUuid(), ce.getEvent().getAuthor(), poll);

        results = dao.getResults(poll.questions());
        assertThat(results).size().isEqualTo(6);
        assertThat(results.get(underTest2)).isEqualTo(1);

        assertThat(results.values().stream().reduce(Integer::sum).orElse(0)).isEqualTo(2);
    }

    @Test(dependsOnMethods = {"testPollCommandSingle", "testPollCommandMultiple"})
    public void testPollCuller() throws Exception {
        PollCullerRunnable r = new PollCullerRunnable(mcHelper);
        // set all poll respondents to 3 days ago
        try (Connection c = mcHelper.getDatabase().getConnection()) {
            int result = Query.of("UPDATE poll_results SET cast_on = cast_on - INTERVAL '7 DAY';")
                    .executeUpdate(c);
            assertThat(dao.getAllPolls()).hasSize(2);
            assertThat(result).isGreaterThan(0); // make sure it actually updated
            r.run();
            assertThat(dao.getAllPolls()).hasSize(1);
            result = Query.of("UPDATE poll SET created_on = created_on - INTERVAL '7 DAY';")
                    .executeUpdate(c);
            r.run();
            assertThat(result).isGreaterThan(0); // make sure it actually updated
            assertThat(dao.getAllPolls()).isEmpty();
        }
    }
}
