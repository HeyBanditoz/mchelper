package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Settings {
    @JsonProperty("discordToken")
    private String discordToken;
    @JsonProperty("botOwners")
    private List<String> botOwners = null;
    @JsonProperty("darkSkyAPI")
    private String darkSkyAPI;
    @JsonProperty("owlBotToken")
    private String owlBotToken;

    @JsonProperty("discordToken")
    public String getDiscordToken() {
        return discordToken;
    }

    @JsonProperty("discordToken")
    public void setDiscordToken(String discordToken) {
        this.discordToken = discordToken;
    }

    @JsonProperty("botOwners")
    public List<String> getBotOwners() {
        return botOwners;
    }

    @JsonProperty("botOwners")
    public void setBotOwners(List<String> botOwners) {
        this.botOwners = botOwners;
    }

    @JsonProperty("darkSkyAPI")
    public String getDarkSkyAPI() {
        return darkSkyAPI;
    }

    @JsonProperty("darkSkyAPI")
    public void setDarkSkyAPI(String darkSkyAPI) {
        this.darkSkyAPI = darkSkyAPI;
    }

    @JsonProperty("owlBotToken")
    public String getOwlBotToken() {
        return owlBotToken;
    }

    @JsonProperty("owlBotToken")
    public void setOwlBotToken(String owlBotToken) {
        this.owlBotToken = owlBotToken;
    }
}
