package io.banditoz.mchelper.utils.database;

import com.zaxxer.hikari.HikariDataSource;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        LOGGER.info("Database loaded. We have " + new GuildConfigDaoImpl(this).getGuildCount() + " guilds in the config.");
    }

    public void migrate() throws Exception {
        LOGGER.info("!!!! RUNNING DATABASE MIGRATIONS !!!!");
        LOGGER.info("Waiting 5 seconds before continuing. If you did not mean to do this, exit now!");
        Thread.sleep(5000);

        try (Connection c = getConnection()) {
            liquibase.database.Database mgDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new Liquibase("sql/postgres/master.yml", new ClassLoaderResourceAccessor(), mgDb);
            liquibase.update(new Contexts(), new LabelExpression());
        }
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
