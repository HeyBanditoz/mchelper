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
    @JsonProperty("recordCommandAndRegexStatistics")
    private Boolean recordCommandAndRegexStatistics = false; // default value, TODO maybe fix others above too?
    @JsonProperty("elasticsearchMessageEndpoint")
    private String elasticsearchMessageEndpoint;
    @JsonProperty("loggedChannels")
    private List<String> loggedChannels;
    @JsonProperty("tarkovMarketApiKey")
    private String tarkovMarketApiKey;
    @JsonProperty("pasteGgApiEndpoint")
    private String pasteGgApiEndpoint;
    @JsonProperty("pasteGgApiKey")
    private String pasteGgApiKey;
    @JsonProperty("pasteGgBaseUrl")
    private String pasteGgBaseUrl;
    @JsonProperty("tarkovToolsApiEndpoint")
    private String tarkovToolsApiEndpoint;
    @JsonProperty("darkSkyEndpoint")
    private String darkSkyEndpoint;
    @JsonProperty("darkSkyApiKey")
    private String darkSkyApiKey;

    private final static Settings defaultSettings = SettingsManager.getDefaultSettings();

    @JsonProperty("discordToken")
    public String getDiscordToken() {
        if (discordToken != null && discordToken.equals(defaultSettings.discordToken)) {
            return null;
        }
        return discordToken;
    }

    @JsonProperty("discordToken")
    public void setDiscordToken(String discordToken) {
        this.discordToken = discordToken;
    }

    @JsonProperty("botOwners")
    public List<String> getBotOwners() {
        if (botOwners != null && botOwners.isEmpty()) {
            return null;
        }
        return botOwners;
    }

    @JsonProperty("botOwners")
    public void setBotOwners(List<String> botOwners) {
        this.botOwners = botOwners;
    }

    @JsonProperty("owlBotToken")
    public String getOwlBotToken() {
        if (owlBotToken != null && owlBotToken.equals(defaultSettings.owlBotToken)) {
            return null;
        }
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
        if (finnhubKey != null && finnhubKey.equals(defaultSettings.finnhubKey)) {
            return null;
        }
        return finnhubKey;
    }

    @JsonProperty("finnhubKey")
    public void setFinnhubKey(String finnhubKey) {
        this.finnhubKey = finnhubKey;
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
        if (elasticsearchMessageEndpoint != null && elasticsearchMessageEndpoint.equals(defaultSettings.elasticsearchMessageEndpoint)) {
            return null;
        }
        return elasticsearchMessageEndpoint;
    }

    @JsonProperty("elasticsearchMessageEndpoint")
    public void setElasticsearchMessageEndpoint(String elasticsearchMessageEndpoint) {
        this.elasticsearchMessageEndpoint = elasticsearchMessageEndpoint;
    }

    @JsonProperty("loggedChannels")
    public List<String> getLoggedChannels() {
        if (loggedChannels != null && loggedChannels.isEmpty()) {
            return null;
        }
        return loggedChannels;
    }

    @JsonProperty("loggedChannels")
    public void setLoggedChannels(List<String> loggedChannels) {
        this.loggedChannels = loggedChannels;
    }

    @JsonProperty("tarkovMarketApiKey")
    public String getTarkovMarketApiKey() {
        if (tarkovMarketApiKey != null && tarkovMarketApiKey.equals(defaultSettings.tarkovMarketApiKey)) {
            return null;
        }
        return tarkovMarketApiKey;
    }

    @JsonProperty("tarkovMarketApiKey")
    public void setTarkovMarketApiKey(String tarkovMarketApiKey) {
        this.tarkovMarketApiKey = tarkovMarketApiKey;
    }

    @JsonProperty("pasteGgApiEndpoint")
    public String getPasteGgApiEndpoint() {

        return pasteGgApiEndpoint;
    }

    @JsonProperty("pasteGgApiEndpoint")
    public void setPasteGgApiEndpoint(String pasteGgApiEndpoint) {
        this.pasteGgApiEndpoint = pasteGgApiEndpoint;
    }

    @JsonProperty("pasteGgApiKey")
    public String getPasteGgApiKey() {
        return pasteGgApiKey;
    }

    @JsonProperty("pasteGgApiKey")
    public void setPasteGgApiKey(String pasteGgApiKey) {
        this.pasteGgApiKey = pasteGgApiKey;
    }

    @JsonProperty("pasteGgApiUrl")
    public String getPasteGgBaseUrl() {
        return pasteGgBaseUrl;
    }

    @JsonProperty("pasteGgApiUrl")
    public void setPasteGgBaseUrl(String pasteGgBaseUrl) {
        this.pasteGgBaseUrl = pasteGgBaseUrl;
    }

    @JsonProperty("tarkovToolsApiEndpoint")
    public String getTarkovToolsApiEndpoint() {
        return tarkovToolsApiEndpoint;
    }

    @JsonProperty("tarkovToolsApiEndpoint")
    public void setTarkovToolsApiEndpoint(String tarkovToolsApiEndpoint) {
        this.tarkovToolsApiEndpoint = tarkovToolsApiEndpoint;
    }

    @JsonProperty("darkSkyEndpoint")
    public String getDarkSkyEndpoint() {
        return darkSkyEndpoint;
    }

    @JsonProperty("darkSkyEndpoint")
    public void setDarkSkyEndpoint(String darkSkyEndpoint) {
        this.darkSkyEndpoint = darkSkyEndpoint;
    }

    @JsonProperty("darkSkyApiKey")
    public String getDarkSkyApiKey() {
        return darkSkyApiKey;
    }

    @JsonProperty("darkSkyApiKey")
    public void setDarkSkyApiKey(String darkSkyApiKey) {
        this.darkSkyApiKey = darkSkyApiKey;
    }
}
