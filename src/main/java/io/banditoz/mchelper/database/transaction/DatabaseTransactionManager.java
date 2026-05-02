package io.banditoz.mchelper.database.transaction;

import java.sql.Connection;

import io.banditoz.mchelper.database.Database;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.banditoz.mchelper.utils.ThrowingCallable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresDatabase
public class DatabaseTransactionManager implements TransactionManager {
    private final Database database;

    @Inject
    public DatabaseTransactionManager(Database database) {
        this.database = database;
    }

    @Override
    public <T> T runInTx(ThrowingCallable<T> work) throws Throwable {
        if (TransactionContext.current() != null) {
            return work.call();
        }

        try (Connection c = database.getRawConnection()) {
            c.setAutoCommit(false);
            TransactionContext.bind(c);
            try {
                T result = work.call();
                c.commit();
                return result;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                TransactionContext.unbind();
                c.setAutoCommit(true);
            }
        }
    }
}
