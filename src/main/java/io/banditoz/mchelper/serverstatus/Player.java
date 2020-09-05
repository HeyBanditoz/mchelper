package io.banditoz.mchelper.serverstatus;

import org.jetbrains.annotations.NotNull;

public class Player implements Comparable<Player> {
    private String name;
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(@NotNull Player p) {
        return this.getName().toLowerCase().compareTo(p.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return name + '(' + id + ')';
    }
}
