package io.banditoz.mchelper.database.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.banditoz.mchelper.database.Poll;
import io.banditoz.mchelper.database.PollQuestion;
import io.banditoz.mchelper.database.PollType;
import io.banditoz.mchelper.database.Question;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface PollsDao {
    Poll createPoll(String title, List<Question> questions, PollType pollType, MessageReceivedEvent e, long botMessageId, UUID closePollUuid) throws SQLException;
    void closePollById(int pollId) throws SQLException;
    void closePollsByMessageIds(List<Long> messageIds) throws SQLException;
    List<Poll> getAllPolls() throws SQLException;
    /**
     * @return true if their vote was added, false otherwise.
     */
    boolean toggleVote(String buttonUuid, User u, Poll p) throws SQLException;
    Map<PollQuestion, Integer> getResults(List<PollQuestion> pqs) throws SQLException;
    List<Poll> getPollsNotRespondedToAfter(long time, TimeUnit unit) throws SQLException;
}
