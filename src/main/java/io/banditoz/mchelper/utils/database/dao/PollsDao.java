package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Poll;
import io.banditoz.mchelper.utils.database.PollQuestion;
import io.banditoz.mchelper.utils.database.PollType;
import io.banditoz.mchelper.utils.database.Question;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
}
