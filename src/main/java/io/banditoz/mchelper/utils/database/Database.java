package io.banditoz.mchelper.utils.database;

import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Database {
    private static Database instance;
    private Guilds guilds;
    private final Path databaseFile = new File(".").toPath().resolve("database.json");
    private final Logger logger = LoggerFactory.getLogger(Database.class);

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Database() {
        if (!databaseFile.toFile().exists()) {
            logger.info("Initializing empty database.");
            this.guilds = initializeEmptyDatabase();
            try {
                saveDatabase();
            } catch (Exception e) {
                logger.error("Error writing default database!", e);
            }
        }
        loadDatabase();
    }

    public void loadDatabase() {
        try {
            this.guilds = MCHelper.getObjectMapper().readValue(databaseFile.toFile(), Guilds.class);
            int numOfGuilds = this.guilds.getGuilds().size();
            logger.info("Database loaded. We have " + numOfGuilds + " guilds.");
        } catch (Exception e) {
            logger.error("Error loading the database.", e);
        }
    }

    public GuildData getGuildDataById(Guild g) throws IllegalStateException {
        GuildData gd;
        if (instance.guilds.getGuilds().get(g.getId()) == null) {
            gd = new GuildData();
            instance.guilds.getGuilds().put(g.getId(), gd);
            logger.info("Initialized data for " + g.getId());
        }
        else {
            gd = instance.guilds.getGuilds().get(g.getId());
        }
        return gd;
    }

    public GuildData getGuildDataNull(Guild g) throws IllegalStateException {
        if (instance.guilds.getGuilds().get(g.getId()) == null) {
            return null;
        }
        else {
            return instance.guilds.getGuilds().get(g.getId());
        }
    }

    public void saveDatabase() {
        try {
            MCHelper.getObjectMapper().writeValue(databaseFile.toFile(), this.guilds);
            logger.debug("Database saved.");
        } catch (IOException e) {
            logger.error("Error saving the database! ", e);
        }
    }

    private Guilds initializeEmptyDatabase() {
        Guilds g = new Guilds();
        return g;
    }
}
