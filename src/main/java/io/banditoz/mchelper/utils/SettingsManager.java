package io.banditoz.mchelper.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Thanks https://github.com/DV8FromTheWorld/Yui/blob/e8da929a8f637591e4da53599c39c8161be38746/src/net/dv8tion//SettingsManager.java
public class SettingsManager {
    private static SettingsManager instance;
    private final ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private Settings settings;
    private final Path configFile = new File(".").toPath().resolve("Config.json");
    private final Logger logger = LoggerFactory.getLogger(SettingsManager.class);

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public SettingsManager() {
        if (!configFile.toFile().exists()) {
            logger.info("Creating default Settings");
            logger.info("You will need to edit the Config.json with your login information.");
            this.settings = getDefaultSettings();
            try {
                saveSettings();
            } catch (Exception e) {
                logger.error("Error writing default settings!", e);
            }
            System.exit(1);
        }
        loadSettings();
    }

    public void loadSettings() {
        try {
            this.settings = om.readValue(configFile.toFile(), Settings.class);
            logger.info("Settings loaded");
            checkSettings();
        } catch (Exception e) {
            logger.error("Error Loading Settings", e);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void saveSettings() throws IOException {
        om.writeValue(configFile.toFile(), this.settings);
    }

    private Settings getDefaultSettings() {
        Settings defaultSettings = new Settings();
        List<String> defaultOwners = new ArrayList<>();
        defaultOwners.add("Bot ID here");
        defaultSettings.setDiscordToken("Bot token here...");
        defaultSettings.setBotOwners(defaultOwners);
        defaultSettings.setDarkSkyAPI("Dark Sky API key here.");
        defaultSettings.setOwlBotToken("OwlBot API key here.");
        defaultSettings.setCommandThreads(2);
        defaultSettings.setRegexListenerThreads(2);
        defaultSettings.setWatchDeletedMessages(false);
        defaultSettings.setAlphaVantageKey("Alpha Vantage API key here");
        return defaultSettings;
    }

    private void checkSettings() {
        if (this.settings.getCommandThreads() < 1) {
            logger.warn("Command threads must be greater than or equal to one! Setting to one for the time being. Check your settings file.");
            this.settings.setCommandThreads(1);
        }
        if (this.settings.getRegexListenerThreads() < 1) {
            logger.warn("Regex listener threads must be greater than or equal to one! Setting to one for the time being. Check your settings file.");
            this.settings.setRegexListenerThreads(1);
        }
    }
}
