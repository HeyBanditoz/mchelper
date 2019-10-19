package io.banditoz.mchelper.utils;

public class Settings {
    private String discordToken;
    private String[] botOwners;
    private String darkSkyAPI;

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

    public String getDarkSkyAPI() {
        return darkSkyAPI;
    }

    public void setDarkSkyAPI(String darkSkyAPI) {
        this.darkSkyAPI = darkSkyAPI;
    }
}
