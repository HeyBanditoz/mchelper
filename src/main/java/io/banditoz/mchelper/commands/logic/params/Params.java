package io.banditoz.mchelper.commands.logic.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.stats.Status;

/** Functional parameter chaining helper. */
public final class Params {
    private Params() {
    }

    /**
     * @return An empty chain. Call {@code arg}, {@code optional} or {@code rest} to add parameters. Each has an
     * overload taking a {@link Completer}, which makes Discord suggest values for that parameter as slash users type.
     */
    public static P0 of() {
        return new P0(List.of());
    }

    /** @return Helper for no-arg command. Equivalent to {@code Params.of().handle(this::go)} */
    public static Handler empty(Fn0 fn) {
        return new P0(List.of()).handle(fn);
    }

    private static List<Arg<?>> plus(List<Arg<?>> existing, Arg<?> next) {
        List<Arg<?>> copy = new ArrayList<>(existing.size() + 1);
        copy.addAll(existing);
        copy.add(next);
        return copy;
    }

    private static <T> Arg<T> req(String name, String desc, ParamType<T> type, @Nullable Completer completer) {
        return new Arg<>(name, desc, type, true, false, completer);
    }

    private static <T> Arg<T> opt(String name, String desc, ParamType<T> type, @Nullable Completer completer) {
        return new Arg<>(name, desc, type, false, false, completer);
    }

    private static Arg<String> greedy(String name, String desc, @Nullable Completer completer) {
        return new Arg<>(name, desc, ParamTypes.STRING, true, true, completer);
    }

    @FunctionalInterface
    public interface Fn0 {
        Status apply(ICommandEvent ce) throws Exception;
    }

    @FunctionalInterface
    public interface Fn1<A> {
        Status apply(ICommandEvent ce, A a) throws Exception;
    }

    @FunctionalInterface
    public interface Fn2<A, B> {
        Status apply(ICommandEvent ce, A a, B b) throws Exception;
    }

    @FunctionalInterface
    public interface Fn3<A, B, C> {
        Status apply(ICommandEvent ce, A a, B b, C c) throws Exception;
    }

    @FunctionalInterface
    public interface Fn4<A, B, C, D> {
        Status apply(ICommandEvent ce, A a, B b, C c, D d) throws Exception;
    }

    @FunctionalInterface
    public interface Fn5<A, B, C, D, E> {
        Status apply(ICommandEvent ce, A a, B b, C c, D d, E e) throws Exception;
    }

    public record P0(List<Arg<?>> args) {
        public <A> P1<A> arg(String name, String desc, ParamType<A> type) {
            return arg(name, desc, type, null);
        }

        public <A> P1<A> arg(String name, String desc, ParamType<A> type, Completer completer) {
            return new P1<>(plus(args, req(name, desc, type, completer)));
        }

        public <A> P1<Optional<A>> optional(String name, String desc, ParamType<A> type) {
            return optional(name, desc, type, null);
        }

        public <A> P1<Optional<A>> optional(String name, String desc, ParamType<A> type, Completer completer) {
            return new P1<>(plus(args, opt(name, desc, type, completer)));
        }

        public Handler rest(String name, String desc, Fn1<String> fn) {
            return rest(name, desc, null, fn);
        }

        public Handler rest(String name, String desc, Completer completer, Fn1<String> fn) {
            return new P1<String>(plus(args, greedy(name, desc, completer))).handle(fn);
        }

        public Handler handle(Fn0 fn) {
            return new Handler(new ParamSpec(args), (ce, v) -> fn.apply(ce));
        }
    }

    public record P1<A>(List<Arg<?>> args) {
        public <B> P2<A, B> arg(String name, String desc, ParamType<B> type) {
            return arg(name, desc, type, null);
        }

        public <B> P2<A, B> arg(String name, String desc, ParamType<B> type, Completer completer) {
            return new P2<>(plus(args, req(name, desc, type, completer)));
        }

        public <B> P2<A, Optional<B>> optional(String name, String desc, ParamType<B> type) {
            return optional(name, desc, type, null);
        }

        public <B> P2<A, Optional<B>> optional(String name, String desc, ParamType<B> type, Completer completer) {
            return new P2<>(plus(args, opt(name, desc, type, completer)));
        }

        public Handler rest(String name, String desc, Fn2<A, String> fn) {
            return rest(name, desc, null, fn);
        }

        public Handler rest(String name, String desc, Completer completer, Fn2<A, String> fn) {
            return new P2<A, String>(plus(args, greedy(name, desc, completer))).handle(fn);
        }

        @SuppressWarnings("unchecked")
        public Handler handle(Fn1<A> fn) {
            return new Handler(new ParamSpec(args), (ce, v) -> fn.apply(ce, (A) v[0]));
        }
    }

    public record P2<A, B>(List<Arg<?>> args) {
        public <C> P3<A, B, C> arg(String name, String desc, ParamType<C> type) {
            return arg(name, desc, type, null);
        }

        public <C> P3<A, B, C> arg(String name, String desc, ParamType<C> type, Completer completer) {
            return new P3<>(plus(args, req(name, desc, type, completer)));
        }

        public <C> P3<A, B, Optional<C>> optional(String name, String desc, ParamType<C> type) {
            return optional(name, desc, type, null);
        }

        public <C> P3<A, B, Optional<C>> optional(String name, String desc, ParamType<C> type, Completer completer) {
            return new P3<>(plus(args, opt(name, desc, type, completer)));
        }

        public Handler rest(String name, String desc, Fn3<A, B, String> fn) {
            return rest(name, desc, null, fn);
        }

        public Handler rest(String name, String desc, Completer completer, Fn3<A, B, String> fn) {
            return new P3<A, B, String>(plus(args, greedy(name, desc, completer))).handle(fn);
        }

        @SuppressWarnings("unchecked")
        public Handler handle(Fn2<A, B> fn) {
            return new Handler(new ParamSpec(args), (ce, v) -> fn.apply(ce, (A) v[0], (B) v[1]));
        }
    }

    public record P3<A, B, C>(List<Arg<?>> args) {
        public <D> P4<A, B, C, D> arg(String name, String desc, ParamType<D> type) {
            return arg(name, desc, type, null);
        }

        public <D> P4<A, B, C, D> arg(String name, String desc, ParamType<D> type, Completer completer) {
            return new P4<>(plus(args, req(name, desc, type, completer)));
        }

        public <D> P4<A, B, C, Optional<D>> optional(String name, String desc, ParamType<D> type) {
            return optional(name, desc, type, null);
        }

        public <D> P4<A, B, C, Optional<D>> optional(String name, String desc, ParamType<D> type, Completer completer) {
            return new P4<>(plus(args, opt(name, desc, type, completer)));
        }

        public Handler rest(String name, String desc, Fn4<A, B, C, String> fn) {
            return rest(name, desc, null, fn);
        }

        public Handler rest(String name, String desc, Completer completer, Fn4<A, B, C, String> fn) {
            return new P4<A, B, C, String>(plus(args, greedy(name, desc, completer))).handle(fn);
        }

        @SuppressWarnings("unchecked")
        public Handler handle(Fn3<A, B, C> fn) {
            return new Handler(new ParamSpec(args), (ce, v) -> fn.apply(ce, (A) v[0], (B) v[1], (C) v[2]));
        }
    }

    public record P4<A, B, C, D>(List<Arg<?>> args) {
        public <E> P5<A, B, C, D, E> arg(String name, String desc, ParamType<E> type) {
            return arg(name, desc, type, null);
        }

        public <E> P5<A, B, C, D, E> arg(String name, String desc, ParamType<E> type, Completer completer) {
            return new P5<>(plus(args, req(name, desc, type, completer)));
        }

        public <E> P5<A, B, C, D, Optional<E>> optional(String name, String desc, ParamType<E> type) {
            return optional(name, desc, type, null);
        }

        public <E> P5<A, B, C, D, Optional<E>> optional(String name, String desc, ParamType<E> type, Completer completer) {
            return new P5<>(plus(args, opt(name, desc, type, completer)));
        }

        public Handler rest(String name, String desc, Fn5<A, B, C, D, String> fn) {
            return rest(name, desc, null, fn);
        }

        public Handler rest(String name, String desc, Completer completer, Fn5<A, B, C, D, String> fn) {
            return new P5<A, B, C, D, String>(plus(args, greedy(name, desc, completer))).handle(fn);
        }

        @SuppressWarnings("unchecked")
        public Handler handle(Fn4<A, B, C, D> fn) {
            return new Handler(new ParamSpec(args),
                    (ce, v) -> fn.apply(ce, (A) v[0], (B) v[1], (C) v[2], (D) v[3]));
        }
    }

    public record P5<A, B, C, D, E>(List<Arg<?>> args) {
        @SuppressWarnings("unchecked")
        public Handler handle(Fn5<A, B, C, D, E> fn) {
            return new Handler(new ParamSpec(args),
                    (ce, v) -> fn.apply(ce, (A) v[0], (B) v[1], (C) v[2], (D) v[3], (E) v[4]));
        }
    }
}
