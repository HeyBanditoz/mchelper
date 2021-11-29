package io.banditoz.mchelper.utils.database;

import com.zaxxer.hikari.HikariDataSource;
import io.banditoz.mchelper.utils.database.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class Database {
    private final HikariDataSource POOL;
    private final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public Database() {
        if (!isConfigured()) {
            throw new IllegalStateException("The database is not configured.");
        }
        Map<String, String> env = System.getenv();
        String url = "jdbc:mariadb://" + env.get("HOST") +
                "/" + env.get("DB") +
                "?user=" + env.get("USER") +
                "&password=" + env.get("PASS");
        POOL = new HikariDataSource();
        POOL.setMaximumPoolSize(2);
        POOL.setDriverClassName("org.mariadb.jdbc.Driver");
        POOL.setJdbcUrl(url);

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
        daos.forEach(Dao::generateTable);
        LOGGER.info("Database loaded. We have " + new GuildConfigDaoImpl(this).getGuildCount() + " guilds in the config.");
    }

    /**
     * @return A new {@link Connection} to use.
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return POOL.getConnection();
    }

    /**
     * Closes the underlying {@link HikariDataSource}
     */
    public void close() {
        POOL.close();
    }

    /**
     * @return If the database environment variables are configured or not. HOST, DB, USER, PASS.
     */
    public static boolean isConfigured() {
        Map<String, String> env = System.getenv();
        return env.containsKey("HOST") && env.containsKey("DB") && env.containsKey("USER") && env.containsKey("PASS");
    }
}
