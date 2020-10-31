package io.banditoz.mchelper.utils;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class EventHandler<E extends GenericEvent> extends ListenerAdapter {
    protected final Map<String, Consumer<E>> events = new HashMap<>();
    private Consumer<GenericEvent> consumer = this::eventConsumer;
    private Class<E> c;

    public EventHandler() {
        this.c = (Class<E>)((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];;
    }

    public void addEvent(String id, Consumer<E> act) {
        events.remove(id);
        events.put(id, act);
    }

    public void removeEvent(String id) {
        events.remove(id);
    }

    public boolean containsEvent(String id) {
        return events.containsKey(id);
    }

    public abstract void eventConsumer(GenericEvent event);

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        if (event.getClass().isAssignableFrom(c)) {
            consumer.accept(event);
        }
    }
}


