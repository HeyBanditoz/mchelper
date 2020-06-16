package io.banditoz.mchelper.utils.database;

import java.sql.Timestamp;

public class GuildConfig {
    private long id;
    private char prefix;
    private long defaultChannel;
    private boolean postQotdToDefaultChannel;
    private Timestamp lastModified;

    public GuildConfig(long id) {
        this.id = id;
        this.prefix = '!';
        this.postQotdToDefaultChannel = false;
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

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }
}
