package io.banditoz.mchelper.utils.database;

import com.zaxxer.hikari.HikariDataSource;
import io.banditoz.mchelper.utils.ClassUtils;
import io.banditoz.mchelper.utils.database.dao.Dao;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class Database {
    private final HikariDataSource POOL;
    private final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public Database() {
        if (!isConfigured()) {
            throw new IllegalStateException("The database is not configured.");
        }
        Map<String, String> env = System.getenv();
        String url = "jdbc:postgresql://" + env.get("HOST") +
                "/" + env.get("DB") +
                "?user=" + env.get("USER") +
                "&password=" + env.get("PASS") +
                "&useSSL=false&currentSchema=" + env.get("SCHEMA");
        POOL = new HikariDataSource();
        POOL.setMaximumPoolSize(2);
        POOL.setJdbcUrl(url);

        try (Connection c = getConnection()) {
            // empty to call close()
        } catch (Exception ex) {
            LOGGER.error("Could not get connection from pool! Cowardly stopping.", ex);
            System.exit(1);
        }

        // we have a connection, generate tables!
        ClassUtils.getAllSubtypesOf(Dao.class).stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    try {
                        clazz.getDeclaredConstructor(Database.class).newInstance(this).generateTable();
                    } catch (Exception ex) {
                        LOGGER.error("Error while instantiating {}!", clazz, ex);
                    }
                });
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
        return env.containsKey("HOST") && env.containsKey("DB") && env.containsKey("USER") && env.containsKey("PASS") && env.containsKey("SCHEMA");
    }
}
