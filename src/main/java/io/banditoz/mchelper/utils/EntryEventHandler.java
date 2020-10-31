package io.banditoz.mchelper.utils;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class EntryEventHandler<T extends Event> extends ListenerAdapter {
    protected final Map<String, Entry<String, Consumer<T>>> events = new HashMap<>();

    public void addEvent(String id, String entryID, Consumer<T> act) {
        events.remove(id);
        events.put(id, new SimpleEntry(entryID,act));
    }

    public void removeEvent(String id) {
        events.remove(id);
    }

    public boolean containsEvent(String id) {
        return events.containsKey(id);
    }
}
