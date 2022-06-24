package io.banditoz.mchelper.games;

import net.dv8tion.jda.api.entities.User;

import java.util.HashSet;
import java.util.Set;

public class GameManager {
    private final Set<User> games = new HashSet<>();

    public void startPlaying(User u) {
        games.add(u);
    }

    public boolean isPlaying(User u) {
        return games.contains(u);
    }

    public void stopPlaying(User u) {
        games.remove(u);
    }
}
