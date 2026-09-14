package io.banditoz.mchelper.telemetry;

import java.util.function.Consumer;

import net.dv8tion.jda.api.requests.RestAction;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@SuppressWarnings("unchecked")
public class RestActionTracingAspect {
    @Around("""
            call(void net.dv8tion.jda.api.requests.RestAction+.queue(..))
             && target(action)
             && !within(io.banditoz.mchelper.telemetry..*)
             && !withincode(@SkipAspect * *(..))""")
    public Object aroundQueue(ProceedingJoinPoint pjp,
                              JoinPoint.EnclosingStaticPart caller,
                              RestAction<Object> action) {
        Object[] args = pjp.getArgs();
        Consumer<Object> success = args.length > 0 ? (Consumer<Object>) args[0] : null;
        Consumer<Throwable> failure = args.length > 1 ? (Consumer<Throwable>) args[1] : null;
        JdaTracing.queue(action, callerName(caller), success, failure);
        return null;
    }

    @Around("""
            call(* net.dv8tion.jda.api.requests.RestAction+.complete(..))
             && target(action)
             && !within(io.banditoz.mchelper.telemetry..*)
             && !withincode(@SkipAspect * *(..))""")
    public Object aroundComplete(ProceedingJoinPoint pjp,
                                 JoinPoint.EnclosingStaticPart caller,
                                 RestAction<?> action) throws Throwable {
        return JdaTracing.complete(action, callerName(caller), pjp::proceed);
    }

    private static String callerName(JoinPoint.EnclosingStaticPart caller) {
        Signature s = caller.getSignature();
        return s.getDeclaringType().getSimpleName() + '#' + s.getName();
    }
}
