package io.banditoz.mchelper.utils.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class Guilds {
    @JsonProperty("guilds")
    private HashMap<String, GuildData> guilds;

    public Guilds() {
        this.guilds = new HashMap<>();
    }

    public HashMap<String, GuildData> getGuilds() {
        return guilds;
    }

    public void setGuilds(HashMap<String, GuildData> guilds) {
        this.guilds = guilds;
    }
}
