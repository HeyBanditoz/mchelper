package io.banditoz.mchelper.database.transaction;

import io.avaje.inject.Secondary;
import io.banditoz.mchelper.utils.ThrowingCallable;
import jakarta.inject.Singleton;

@Singleton
@Secondary
public class NoopTransactionManager implements TransactionManager {
    @Override
    public <T> T runInTx(ThrowingCallable<T> work) throws Throwable {
        return work.call();
    }
}
