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
    @JsonProperty("grafanaUrl")
    private String grafanaUrl;
    @JsonProperty("esUrl")
    private String esUrl;
    @JsonProperty("grafanaToken")
    private String grafanaToken;
    @JsonProperty("commandThreads")
    private Integer commandThreads;
    @JsonProperty("regexListenerThreads")
    private Integer regexListenerThreads;
    @JsonProperty("watchDeletedMessages")
    private Boolean watchDeletedMessages;

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

    @JsonProperty("grafanaUrl")
    public String getGrafanaUrl() {
        return grafanaUrl;
    }

    @JsonProperty("grafanaUrl")
    public void setGrafanaUrl(String grafanaUrl) {
        this.grafanaUrl = grafanaUrl;
    }

    @JsonProperty("esUrl")
    public String getEsUrl() {
        return esUrl;
    }

    @JsonProperty("esUrl")
    public void setEsUrl(String esUrl) {
        this.esUrl = esUrl;
    }

    @JsonProperty("grafanaToken")
    public String getGrafanaToken() {
        return grafanaToken;
    }

    @JsonProperty("grafanaToken")
    public void setGrafanaToken(String grafanaToken) {
        this.grafanaToken = grafanaToken;
    }

    @JsonProperty("commandThreads")
    public int getCommandThreads() {
        return commandThreads;
    }

    @JsonProperty("commandThreads")
    public void setCommandThreads(int commandThreads) {
        this.commandThreads = commandThreads;
    }

    @JsonProperty("regexListenerThreads")
    public int getRegexListenerThreads() {
        return regexListenerThreads;
    }

    @JsonProperty("regexListenerThreads")
    public void setRegexListenerThreads(Integer regexListenerThreads) {
        this.regexListenerThreads = regexListenerThreads;
    }

    @JsonProperty("watchDeletedMessages")
    public Boolean getWatchDeletedMessages() {
        return watchDeletedMessages;
    }

    @JsonProperty("watchDeletedMessages")
    public void setWatchDeletedMessages(Boolean watchDeletedMessages) {
        this.watchDeletedMessages = watchDeletedMessages;
    }
}
