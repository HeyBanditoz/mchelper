package io.banditoz.mchelper.stats;

/** Class which holds how a command was executed. */
public enum Kind {
    /** This command was invoked via text (i.e. !pick). */
    TEXT,
    /** The command was originally invoked via text (i.e. !pick), and was re-processed into the command handler. */
    TEXT_REPLAY
}
