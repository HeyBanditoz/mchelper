package io.banditoz.mchelper.database.transaction;

import java.sql.Connection;

import io.banditoz.mchelper.utils.ThrowingCallable;

public interface TransactionManager {

    /**
     * Runs some code and returns the result in a single database transaction; the caller does not have to worry about
     * managing a SQL {@link Connection}.
     *
     * @param work The block of code to run.
     */
    <T> T runInTx(ThrowingCallable<T> work) throws Throwable;

    /**
     * Runs some code in a single database transaction; the caller does not have to worry about managing a SQL
     * {@link Connection}.
     *
     * @param work The block of code to run.
     */
    default void runInTx(Runnable work) throws Throwable {
        runInTx(() -> {
            work.run();
            return null;
        });
    }
}
