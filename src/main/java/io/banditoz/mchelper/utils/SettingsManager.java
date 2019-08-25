package io.banditoz.mchelper.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Thanks https://github.com/DV8FromTheWorld/Yui/blob/e8da929a8f637591e4da53599c39c8161be38746/src/net/dv8tion//SettingsManager.java
public class SettingsManager {
    private static SettingsManager instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Settings Settings;
    private final Path configFile = new File(".").toPath().resolve("Config.json");

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public SettingsManager() {
        if (!configFile.toFile().exists()) {
            System.out.println("SettingsManager: Creating default Settings");
            System.out.println("SettingsManager: You will need to edit the Config.json with your login information.");
            this.Settings = getDefaultSettings();
            saveSettings();
            System.exit(1);
        }
        loadSettings();
    }

    public void loadSettings() {
        try {
            BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
            this.Settings = gson.fromJson(reader, Settings.class);
            reader.close();
            System.out.println("SettingsManager: Settings loaded");
        } catch (IOException e) {
            System.out.println("SettingsManager: Error Loading Settings");
            e.printStackTrace();
        }
    }

    public Settings getSettings() {
        return Settings;
    }

    public void saveSettings() {
        String jsonOut = gson.toJson(this.Settings);
        try {
            BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8);
            writer.append(jsonOut);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Settings getDefaultSettings() {
        Settings defaultSettings = new Settings();
        defaultSettings.setDiscordToken("Bot Token Here");
        defaultSettings.setBotOwners(new String[]{"ID Here"});
        return defaultSettings;
    }
}
