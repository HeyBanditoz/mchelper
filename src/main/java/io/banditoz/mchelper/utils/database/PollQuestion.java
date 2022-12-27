package io.banditoz.mchelper.utils.database;

public record PollQuestion(int id, int pollId, String question, short questionNumber, String buttonUuid) {
}
