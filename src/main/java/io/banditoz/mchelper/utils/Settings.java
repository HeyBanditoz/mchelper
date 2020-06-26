package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
    @JsonProperty("discordToken")
    private String discordToken;
    @JsonProperty("botOwners")
    private List<String> botOwners = null;
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
    @JsonProperty("watchDeletedMessages")
    private Boolean watchDeletedMessages;
    @JsonProperty("alphaVantageKey")
    private String alphaVantageKey;
    @JsonProperty("riotApiKey")
    private String riotApiKey;
    @JsonProperty("databaseUsername")
    private String databaseUsername;
    @JsonProperty("databasePassword")
    private String databasePassword;
    @JsonProperty("databaseHostAndPort")
    private String databaseHostAndPort;
    @JsonProperty("databaseName")
    private String databaseName;

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

    @JsonProperty("watchDeletedMessages")
    public Boolean getWatchDeletedMessages() {
        return watchDeletedMessages;
    }

    @JsonProperty("watchDeletedMessages")
    public void setWatchDeletedMessages(Boolean watchDeletedMessages) {
        this.watchDeletedMessages = watchDeletedMessages;
    }

    @JsonProperty("alphaVantageKey")
    public String getAlphaVantageKey() {
        return alphaVantageKey;
    }

    @JsonProperty("alphaVantageKey")
    public void setAlphaVantageKey(String alphaVantageKey) {
        this.alphaVantageKey = alphaVantageKey;
    }

    @JsonProperty("riotApiKey")
    public String getRiotApiKey() {
        return riotApiKey;
    }

    @JsonProperty("riotApiKey")
    public void setRiotApiKey(String riotApiKey) {
        this.riotApiKey = riotApiKey;
    }

    @JsonProperty("databaseUsername")
    public String getDatabaseUsername() {
        return databaseUsername;
    }

    @JsonProperty("databaseUsername")
    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    @JsonProperty("databasePassword")
    public String getDatabasePassword() {
        return databasePassword;
    }

    @JsonProperty("databasePassword")
    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    @JsonProperty("databaseHostAndPort")
    public String getDatabaseHostAndPort() {
        return databaseHostAndPort;
    }

    @JsonProperty("databaseHostAndPort")
    public void setDatabaseHostAndPort(String databaseHostAndPort) {
        this.databaseHostAndPort = databaseHostAndPort;
    }

    @JsonProperty("databaseName")
    public String getDatabaseName() {
        return databaseName;
    }

    @JsonProperty("databaseName")
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
