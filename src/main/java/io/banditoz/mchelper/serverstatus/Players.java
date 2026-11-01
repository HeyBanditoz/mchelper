package io.banditoz.mchelper.serverstatus;

import java.util.List;

public record Players(int max, int online, List<Player> sample) {
    public Players {
        sample = sample == null ? List.of() : sample;
    }
}
