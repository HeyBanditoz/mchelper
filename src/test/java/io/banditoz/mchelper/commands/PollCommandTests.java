package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.PollService;
import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.database.Poll;
import io.banditoz.mchelper.database.PollQuestion;
import io.banditoz.mchelper.database.dao.PollsDao;
import io.banditoz.mchelper.runnables.PollCullerRunnable;
import io.jenetics.facilejdbc.Query;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@InjectTest
class PollCommandTests extends BaseCommandTest {
    @Inject
    PollCommand pc;
    @Inject
    PollsDao dao;
    @Inject
    PollService pollService;
    @Inject
    Database database;

    @BeforeEach
    void clear() {
        truncate("poll", "poll_question", "poll_results");
        resetSequence("poll_id_seq", "poll_question_id_seq");
    }

    // TODO expand these tests. They're kinda barebones.
    @Test
    void testPollCommandSingle() throws Exception {
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
    void testPollCommandMultiple() throws Exception {
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

    @Test
    void testPollCuller() throws Exception {
        Message m = mock(Message.class);
        when(m.getIdLong()).thenReturn(100L);
        when(ce.getEvent().getChannel().sendMessage(any(MessageCreateData.class)).complete()).thenReturn(m);

        setArgs("\"Best chicken nugget\" single Fried Raw \"Double Fried\" Baked \"Air Fried\" \"Heated with Radiation\"");
        pc.onCommand(ce);
        Poll poll = dao.getAllPolls().get(0);
        Map<PollQuestion, Integer> results = dao.getResults(poll.questions());
        List<PollQuestion> pqs = results.keySet().stream().toList();
        PollQuestion underTest = pqs.get(0);
        dao.toggleVote(underTest.buttonUuid(), ce.getEvent().getAuthor(), poll);

        PollCullerRunnable r = new PollCullerRunnable(pollService, dao, mock(JDA.class));
        // set all poll respondents to 3 days ago
        try (Connection c = database.getConnection()) {
            int result = Query.of("UPDATE poll_results SET cast_on = cast_on - INTERVAL '7 DAY';")
                    .executeUpdate(c);
            assertThat(dao.getAllPolls()).hasSize(1);
            assertThat(result).isGreaterThan(0); // make sure it actually updated
            result = Query.of("UPDATE poll SET created_on = created_on - INTERVAL '7 DAY';")
                    .executeUpdate(c);
            r.run();
            assertThat(result).isGreaterThan(0); // make sure it actually updated
            assertThat(dao.getAllPolls()).isEmpty();
        }
    }
}
