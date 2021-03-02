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
    @JsonProperty("commandThreads")
    private Integer commandThreads;
    @JsonProperty("finnhubKey")
    private String finnhubKey;
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
    @JsonProperty("recordCommandAndRegexStatistics")
    private Boolean recordCommandAndRegexStatistics = false; // default value, TODO maybe fix others above too?
    @JsonProperty("elasticsearchMessageEndpoint")
    private String elasticsearchMessageEndpoint;
    @JsonProperty("loggedChannels")
    private List<String> loggedChannels;

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

    @JsonProperty("commandThreads")
    public int getCommandThreads() {
        return commandThreads;
    }

    @JsonProperty("commandThreads")
    public void setCommandThreads(int commandThreads) {
        this.commandThreads = commandThreads;
    }

    @JsonProperty("finnhubKey")
    public String getFinnhubKey() {
        return finnhubKey;
    }

    @JsonProperty("finnhubKey")
    public void setFinnhubKey(String finnhubKey) {
        this.finnhubKey = finnhubKey;
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

    @JsonProperty("recordCommandAndRegexStatistics")
    public Boolean getRecordCommandAndRegexStatistics() {
        return recordCommandAndRegexStatistics;
    }

    @JsonProperty("recordCommandAndRegexStatistics")
    public void setRecordCommandAndRegexStatistics(Boolean recordCommandAndRegexStatistics) {
        this.recordCommandAndRegexStatistics = recordCommandAndRegexStatistics;
    }

    @JsonProperty("elasticsearchMessageEndpoint")
    public String getElasticsearchMessageEndpoint() {
        return elasticsearchMessageEndpoint;
    }

    @JsonProperty("elasticsearchMessageEndpoint")
    public void setElasticsearchMessageEndpoint(String elasticsearchMessageEndpoint) {
        this.elasticsearchMessageEndpoint = elasticsearchMessageEndpoint;
    }

    @JsonProperty("loggedChannels")
    public List<String> getLoggedChannels() {
        return loggedChannels;
    }

    @JsonProperty("loggedChannels")
    public void setLoggedChannels(List<String> loggedChannels) {
        this.loggedChannels = loggedChannels;
    }
}
