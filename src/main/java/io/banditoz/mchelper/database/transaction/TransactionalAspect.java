package io.banditoz.mchelper.database.transaction;

import java.lang.reflect.Method;

import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.aop.MethodInterceptor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TransactionalAspect implements AspectProvider<Transactional> {
    private final TransactionManager transactionManager;

    @Inject
    public TransactionalAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public MethodInterceptor interceptor(Method method, Transactional aspectAnnotation) {
        return invocation -> transactionManager.runInTx(invocation::invoke);
    }
}
