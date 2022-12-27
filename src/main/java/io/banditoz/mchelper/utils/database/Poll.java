package io.banditoz.mchelper.utils.database;

import java.sql.Timestamp;
import java.util.List;

public record Poll(
        int id,
        String title,
        long channelId,
        long messageId,
        long authorId,
        boolean closed,
        String closedButtonUuid,
        Timestamp createdOn,
        PollType type,
        List<PollQuestion> questions
) {
}
