package io.banditoz.mchelper.database.transaction;

import java.sql.Connection;

/**
 * Allows threads to be aware of an active database transaction.
 * Should only be used by the {@link DatabaseTransactionManager}!
 */
public class TransactionContext {
    private static final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

    static void bind(Connection c) {
        transactionConnection.set(c);
    }

    static void unbind() {
        transactionConnection.remove();
    }

    public static Connection current() {
        return transactionConnection.get();
    }

    private TransactionContext() {}
}
