package io.banditoz.mchelper.utils;

public class Settings {
    private String discordToken;
    private String[] botOwners;
    private String coinMarketCapAPIKey;

    public String getCoinMarketCapAPIKey() {
        return coinMarketCapAPIKey;
    }

    public void setCoinMarketCapAPIKey(String coinMarketCapAPIKey) {
        this.coinMarketCapAPIKey = coinMarketCapAPIKey;
    }

    public String[] getBotOwners() {
        return botOwners;
    }

    public void setBotOwners(String[] botOwners) {
        this.botOwners = botOwners;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public void setDiscordToken(String discordToken) {
        this.discordToken = discordToken;
    }

}
