package io.banditoz.mchelper.utils.database;

public enum PollType {
    /** One user gets one vote for many options. */
    SINGLE,
    /** One user can vote on all options. */
    MULTIPLE;

    public String nameAsCapitalized() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
