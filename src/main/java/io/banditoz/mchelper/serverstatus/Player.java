package io.banditoz.mchelper.serverstatus;

import org.jetbrains.annotations.NotNull;

public record Player(String name, String id) implements Comparable<Player> {
    @Override
    public int compareTo(@NotNull Player p) {
        return name.toLowerCase().compareTo(p.name.toLowerCase());
    }

    @Override
    public String toString() {
        return name + '(' + id + ')';
    }
}
