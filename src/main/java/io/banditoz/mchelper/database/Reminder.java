package io.banditoz.mchelper.database;

import java.sql.Timestamp;

/**
 * Class which represents a single reminder from the database. Used for scheduling reminders that will notify users of
 * something in the future.
 *
 * @see io.banditoz.mchelper.ReminderService
 * @see io.banditoz.mchelper.commands.RemindmeCommand
 */
public class Reminder {
    /** The ID of the reminder. */
    private int id;
    /** The ID of the channel the reminder came from. */
    private long channelId;
    /** The ID of the reminder's author. */
    private long authorId;
    /** The reminder text. */
    private String reminder;
    /** A Timestamp of when we should send out the reminder. */
    private Timestamp remindWhen;
    /** Whether or not sending out the reminder was successful. */
    private boolean reminded;
    /** Whether or not the reminder originated from a private message to the bot. */
    private boolean isFromDm;
    /** Whether or not the user (or someone else) deleted the reminder. */
    private boolean deleted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public Timestamp getRemindWhen() {
        return remindWhen;
    }

    public void setRemindWhen(Timestamp remindWhen) {
        this.remindWhen = remindWhen;
    }

    public boolean isReminded() {
        return reminded;
    }

    public void setReminded(boolean reminded) {
        this.reminded = reminded;
    }

    public boolean isFromDm() {
        return isFromDm;
    }

    public void setIsFromDm(boolean fromDm) {
        isFromDm = fromDm;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", channelId=" + channelId +
                ", authorId=" + authorId +
                ", reminder='" + reminder + '\'' +
                ", remindWhen=" + remindWhen +
                ", reminded=" + reminded +
                ", isFromDm=" + isFromDm +
                ", deleted=" + deleted +
                '}';
    }
}
