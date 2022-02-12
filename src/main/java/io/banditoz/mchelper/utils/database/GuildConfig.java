package io.banditoz.mchelper.utils.database;

import java.sql.Timestamp;

public class GuildConfig {
    /** The ID of the Guild this GuildConfig represents. */
    private long id;
    /** The prefix for all commands in this Guild. */
    private char prefix;
    /** The default channel, to send the quote of the day and/or guild leave/join notifications. */
    private long defaultChannel;
    /** Whether or not we should deliver the quote of the day to the default channel. */
    private boolean postQotdToDefaultChannel;
    /** The chance for dad bot to fire when someone says `I'm` or `Imma` or `I am` or `Im`. */
    private double dadBotChance;
    /** The chance for bet bot to fire when someone says `bet`. */
    private double betBotChance;
    private Timestamp lastModified;

    public GuildConfig(long id) {
        this.id = id;
        this.prefix = '!';
        this.postQotdToDefaultChannel = false;
        this.dadBotChance = 0;
        this.betBotChance = 0;
    }

    public GuildConfig() {
        prefix = '!'; // for sanity
    }

    public long getId() {
        if (id == 0) {
            throw new IllegalStateException("Cannot get a null guild ID!");
        }
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public char getPrefix() {
        return prefix;
    }

    public void setPrefix(char prefix) {
        this.prefix = prefix;
    }

    public long getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(long defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public boolean getPostQotdToDefaultChannel() {
        return postQotdToDefaultChannel;
    }

    public void setPostQotdToDefaultChannel(boolean postQotdToDefaultChannel) {
        this.postQotdToDefaultChannel = postQotdToDefaultChannel;
    }

    public double getDadBotChance() {
        return dadBotChance;
    }

    public void setDadBotChance(double dadBotChance) {
        this.dadBotChance = dadBotChance;
    }

    public double getBetBotChance() {
        return betBotChance;
    }

    public void setBetBotChance(double betBotChance) {
        this.betBotChance = betBotChance;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }
}
