package io.banditoz.mchelper.serverstatus;

import java.util.ArrayList;
import java.util.List;

public class Players {
    private int max;
    private int online;
    private List<Player> sample = new ArrayList<>();

    public int getMax() {
        return max;
    }

    public int getOnline() {
        return online;
    }

    public List<Player> getSample() {
        return sample;
    }
}
