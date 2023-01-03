package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.*;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PollsDaoImpl extends Dao implements PollsDao {
    public PollsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public Poll createPoll(String title, List<Question> questions, PollType pollType, MessageReceivedEvent e, long botMessageId, UUID closePollUuid) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            c.setAutoCommit(false);
            int pollId = Query.of("INSERT INTO poll (title, poll_type, channel_id, message_id, author_id, close_button_uuid) VALUES (:t, :pt, :c, :m, :a, :u) RETURNING id;")
                    .on(
                            Param.value("t", title),
                            Param.value("pt", pollType.ordinal()),
                            Param.value("c", e.getChannel().getIdLong()),
                            // TODO yeah this is hacky, need to rewrite this so it doesn't need to insert null for message ID we'll insert later after we send the message.
                            Param.value("m", botMessageId),
                            Param.value("a", e.getAuthor().getIdLong()),
                            Param.value("u", closePollUuid)
                    ).as((rs, conn) -> {
                        rs.next();
                        return rs.getInt(1);
                    }, c);
            List<PollQuestion> pqs = new ArrayList<>();
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                int finalI = i;
                pqs.add(Query.of("INSERT INTO poll_question (poll_id, question, question_number, button_uuid) VALUES (:a, :q, :n, :u::uuid) RETURNING id;")
                        .on(
                                Param.value("a", pollId),
                                Param.value("q", q.question()),
                                Param.value("n", i),
                                Param.value("u", q.buttonUuid())
                        ).as((rs, conn) -> {
                            rs.next();
                            return new PollQuestion(rs.getInt(1), pollId, q.question(), (short) (finalI), q.buttonUuid());
                        }, c));
            }
            c.commit();
            c.setAutoCommit(true);
            return new Poll(pollId, title, e.getChannel().getIdLong(), e.getMessageIdLong(), e.getAuthor().getIdLong(), false, closePollUuid.toString(), Timestamp.from(Instant.now()), pollType, pqs);
        }
    }

    @Override
    public void closePollById(int pollId) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("UPDATE poll SET closed = true WHERE id = :i")
                    .on(Param.value("i", pollId))
                    .executeUpdate(c);
        }
    }

    @Override
    public void closePollsByMessageIds(List<Long> messageIds) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("UPDATE poll SET closed = true WHERE message_id IN (:i);")
                    .on(Param.values("i", messageIds))
                    .executeUpdate(c);
        }
    }

    @Override
    public List<Poll> getAllPolls() throws SQLException {
        List<Poll> polls = new ArrayList<>();
        try (Connection c = DATABASE.getConnection()) {
            List<Poll> fetchedPolls = Query.of("SELECT * FROM poll WHERE closed = false ORDER BY created_on DESC;")
                    .as((rs, conn) -> parseMany(rs, c, this::parseOnePollSansQuestions), c);
            List<PollQuestion> allPqs = Query.of("""
                            SELECT *
                            FROM poll_question
                            INNER JOIN poll p ON p.id = poll_question.poll_id
                            WHERE closed = false
                            ORDER BY question_number;
                            """)
                    .as((rs, conn) -> parseMany(rs, c, this::parseOnePollQuestion), c);
            for (Poll p : fetchedPolls) {
                List<PollQuestion> pqsForPoll = allPqs.stream().filter(pq -> pq.pollId() == p.id()).toList();
                polls.add(new Poll(p.id(), p.title(), p.channelId(), p.messageId(), p.authorId(), p.closed(), p.closedButtonUuid(), p.createdOn(), p.type(), pqsForPoll));
            }
        }
        return polls;
    }

    @Override
    public boolean toggleVote(String buttonUuid, User u, Poll p) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            int pqId = p.questions().stream()
                    .filter(pollQuestion -> pollQuestion.buttonUuid().equals(buttonUuid))
                    .findAny()
                    .orElseThrow(() -> new NoSuchElementException("Button ID was not associated with any PollQuestion."))
                    .id();
//            int pqId = Query.of("SELECT id FROM poll_question WHERE button_uuid=:b::uuid;")
//                    .on(Param.value("b", buttonUuid))
//                    .as((rs, conn) -> {
//                        rs.next();
//                        return rs.getInt(1);
//                    }, c);
            return Query.of("SELECT toggle_poll_result(:u, :p, :t, :i);")
                    .on(
                            Param.value("u", u.getIdLong()),
                            Param.value("p", pqId),
                            Param.value("t", (short) p.type().ordinal()),
                            Param.value("i", p.id())
                    ).as((rs, conn) -> {
                        rs.next();
                        return rs.getBoolean(1);
                    }, c);
        }
    }

    @Override
    public Map<PollQuestion, Integer> getResults(List<PollQuestion> pqs) throws SQLException {
        // TODO optimize this algorithm a bit
        Map<Integer, PollQuestion> pqsMap = new HashMap<>();
        for (PollQuestion pq : pqs) {
            pqsMap.put(pq.id(), pq);
        }
        try (Connection c = DATABASE.getConnection()) {
            Map<PollQuestion, Integer> pollResults = Query.of("SELECT pq_id, COUNT(user_id) AS count FROM poll_results WHERE pq_id IN (:ids) GROUP BY pq_id;")
                    .on(Param.values("ids", pqs.stream().map(PollQuestion::id).toList()))
                    .as((rs, conn) -> {
                        Map<PollQuestion, Integer> results = new HashMap<>();
                        while (rs.next()) {
                            results.put(pqsMap.get(rs.getInt("pq_id")), rs.getInt("count"));
                        }
                        return results;
                    }, c);
            for (PollQuestion pq : pqs) {
                if (!pollResults.containsKey(pq)) {
                    // no votes for the poll question in question, add it as zero votes
                    pollResults.put(pq, 0);
                }
            }
            return pollResults;
        }
    }

    @Override
    public List<Poll> getPollsNotRespondedToAfter(long time, TimeUnit unit) throws SQLException {
        long timeInSeconds = unit.toSeconds(time);
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("""
                            SELECT DISTINCT(p.*)
                            FROM poll p
                                     INNER JOIN poll_question pq on p.id = pq.poll_id
                                     INNER JOIN poll_results pr on pq.id = pr.pq_id
                            WHERE pr.cast_on <= (NOW() - (INTERVAL '1 SECOND' * :s))
                              AND p.closed = false
                            UNION
                            SELECT p.*
                            FROM poll p
                            WHERE p.created_on <= (NOW() - (INTERVAL '1 SECOND' * :s))
                              AND NOT EXISTS(SELECT pr.pq_id
                                             FROM poll_results pr
                                                      INNER JOIN poll_question pq on pq.id = pr.pq_id
                                             WHERE pq.poll_id = p.id);""")
                    .on(Param.value("s", timeInSeconds))
                    .as((rs, conn) -> parseMany(rs, c, this::parseOnePollSansQuestions), c);
        }
    }

    private @Nullable Poll parseOnePollSansQuestions(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return new Poll(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getLong("channel_id"),
                rs.getLong("message_id"),
                rs.getLong("author_id"),
                rs.getBoolean("closed"),
                rs.getString("close_button_uuid"),
                rs.getTimestamp("created_on"),
                PollType.values()[rs.getInt("poll_type")],
                null
        );
    }

    private @Nullable PollQuestion parseOnePollQuestion(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return new PollQuestion(
                rs.getInt("id"),
                rs.getInt("poll_id"),
                rs.getString("question"),
                rs.getShort("question_number"),
                rs.getString("button_uuid")
        );
    }
}
