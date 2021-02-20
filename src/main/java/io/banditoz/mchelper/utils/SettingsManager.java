package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Thanks https://github.com/DV8FromTheWorld/Yui/blob/e8da929a8f637591e4da53599c39c8161be38746/src/net/dv8tion//SettingsManager.java
public class SettingsManager {
    private Settings settings;
    private final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path CONFIG_FILE;
    private final Logger LOGGER = LoggerFactory.getLogger(SettingsManager.class);


    public SettingsManager(Path path) {
        this.CONFIG_FILE = path;
        if (!CONFIG_FILE.toFile().exists()) {
            LOGGER.info("Creating default Settings");
            LOGGER.info("You will need to edit the " + path.getFileName() + " file with your login information.");
            this.settings = getDefaultSettings();
            try {
                saveSettings();
            } catch (Exception e) {
                LOGGER.error("Error writing default settings!", e);
            }
            System.exit(1);
        }
        loadSettings();
    }

    public void loadSettings() {
        try {
            this.settings = OM.readValue(CONFIG_FILE.toFile(), Settings.class);
            LOGGER.info("Settings loaded");
            checkSettings();
        } catch (Exception e) {
            LOGGER.error("Error Loading Settings", e);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void saveSettings() throws IOException {
        OM.writeValue(CONFIG_FILE.toFile(), this.settings);
    }

    private Settings getDefaultSettings() {
        Settings defaultSettings = new Settings();
        List<String> defaultOwners = new ArrayList<>();
        defaultOwners.add("Bot ID here");
        defaultSettings.setDiscordToken("Bot token here...");
        defaultSettings.setBotOwners(defaultOwners);
        defaultSettings.setOwlBotToken("OwlBot API key here.");
        defaultSettings.setCommandThreads(2);
        defaultSettings.setFinnhubKey("Alpha Vantage API key here.");
        defaultSettings.setRiotApiKey("Riot Api Key here.");
        defaultSettings.setDatabaseName("Name of the database.");
        defaultSettings.setDatabaseHostAndPort("Host and port of the database.");
        defaultSettings.setDatabaseUsername("Database username.");
        defaultSettings.setDatabasePassword("Database password.");
        defaultSettings.setRecordCommandAndRegexStatistics(true);
        return defaultSettings;
    }

    private void checkSettings() {
        if (this.settings.getCommandThreads() < 1) {
            LOGGER.warn("Command threads must be greater than or equal to one! Setting to one for the time being. Check your settings file.");
            this.settings.setCommandThreads(1);
        }
    }
}
