package io.banditoz.mchelper.utils.database;

import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.dao.*;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Database {
    private final MariaDbPoolDataSource POOL;
    private final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public Database(Settings settings) {
        String url = "jdbc:mariadb://" + settings.getDatabaseHostAndPort() +
                "/" + settings.getDatabaseName() +
                "?user=" + settings.getDatabaseUsername() +
                "&password=" + settings.getDatabasePassword() +
                "&useUnicode=true&pool&maxPoolSize=5&maxIdleTime=1800";
        POOL = new MariaDbPoolDataSource(url);

        // we have a connection, generate tables!
        ArrayList<Dao> daos = new ArrayList<>();
        daos.add(new GuildConfigDaoImpl(this));
        daos.add(new CoordsDaoImpl(this));
        daos.add(new RemindersDaoImpl(this));
        daos.add(new QuotesDaoImpl(this));
        daos.add(new CompanyProfileDaoImpl(this));
        daos.add(new StatisticsDaoImpl(this));
        daos.add(new RolesDaoImpl(this));
        daos.add(new AccountsDaoImpl(this));
        daos.add(new TasksDaoImpl(this));
        daos.add(new UserCacheDaoImpl(this));
        daos.add(new TimeZoneInfoDaoImpl(this));
        daos.forEach(Dao::generateTable);
        LOGGER.info("Database loaded. We have " + new GuildConfigDaoImpl(this).getGuildCount() + " guilds.");
    }

    public Connection getConnection() throws SQLException {
        return POOL.getConnection();
    }
}
