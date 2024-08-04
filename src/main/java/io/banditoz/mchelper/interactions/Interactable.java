package io.banditoz.mchelper.interactions;

import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;

public abstract class Interactable<T, E extends Interaction> {
    /** Whether the user can interact with the entire {@link Interactable} or not. */
    protected final Predicate<User> canInteract;
    protected volatile ScheduledFuture<?> timeoutFuture;
    private final long timeoutSeconds;

    public abstract boolean contains(T t);
    public abstract void handleEvent(E e);

    public Interactable(Predicate<User> canInteract, long timeoutSeconds) {
        this.canInteract = canInteract;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void destroy() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
        }
    }

    public ScheduledFuture<?> getTimeoutFuture() {
        return timeoutFuture;
    }

    public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }

    /** @return The timeout of this {@link Interactable}, in seconds. */
    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public boolean test(E e) {
        return canInteract == null || canInteract.test(e.getUser());
    }
}
