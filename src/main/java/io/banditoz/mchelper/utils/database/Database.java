package io.banditoz.mchelper.utils.database;

import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.dao.Dao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {
    private static Connection connection;
    private static Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public static void initializeDatabase() {
        Settings settings = SettingsManager.getInstance().getSettings();
        String url = "jdbc:mariadb://" + settings.getDatabaseHostAndPort() +
                "/" + settings.getDatabaseName() +
                "?user=" + settings.getDatabaseUsername() +
                "&password=" + settings.getDatabasePassword() + "" +
                "&useUnicode=true";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            LOGGER.error("Could not initialize a SQL connection! Things will most likely be broken.", e);
        }

        // we have a connection, generate tables!
        ArrayList<Dao> daos = new ArrayList<>();
        daos.add(new GuildConfigDaoImpl());
        for (Dao dao : daos) {
            dao.generateTable();
        }
        LOGGER.info("Database loaded. We have " + new GuildConfigDaoImpl().getAllGuildConfigs().size() + " guilds.");
    }


    public static Connection getConnection() {
        return connection;
    }
}
