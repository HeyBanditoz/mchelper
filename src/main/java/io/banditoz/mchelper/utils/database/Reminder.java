package io.banditoz.mchelper.utils.database;

import java.sql.Timestamp;

public class Reminder {
    private int id;
    private long channelId;
    private long authorId;
    private String reminder;
    private Timestamp remindWhen;
    private boolean reminded;
    private boolean isFromDm;
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
