package io.banditoz.mchelper.database;

public record PollQuestion(int id, int pollId, String question, short questionNumber, String buttonUuid) {
}
