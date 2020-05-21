package io.banditoz.mchelper.utils.database;

import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.dao.*;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {
    private static MariaDbPoolDataSource pool;
    private static Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public static void initializeDatabase() {
        Settings settings = SettingsManager.getInstance().getSettings();
        String url = "jdbc:mariadb://" + settings.getDatabaseHostAndPort() +
                "/" + settings.getDatabaseName() +
                "?user=" + settings.getDatabaseUsername() +
                "&password=" + settings.getDatabasePassword() + "" +
                "&useUnicode=true&pool&maxPoolSize=2";
        pool = new MariaDbPoolDataSource(url);

        // we have a connection, generate tables!
        ArrayList<Dao> daos = new ArrayList<>();
        daos.add(new GuildConfigDaoImpl());
        daos.add(new CoordsDaoImpl());
        daos.add(new RemindersDaoImpl());
        daos.add(new QuotesDaoImpl());
        daos.forEach(Dao::generateTable);
        LOGGER.info("Database loaded. We have " + new GuildConfigDaoImpl().getGuildCount() + " guilds.");
    }


    public static Connection getConnection() throws SQLException {
        return pool.getConnection();
    }
}
