package io.banditoz.mchelper.utils.database;

import com.zaxxer.hikari.HikariDataSource;
import io.jenetics.facilejdbc.Query;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.hikaricp.v3_0.HikariTelemetry;
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

    public Database(OpenTelemetry openTelemetry) {
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
        POOL.setMetricsTrackerFactory(HikariTelemetry.create(openTelemetry).createMetricsTrackerFactory());
        POOL.setJdbcUrl(url);

        try (Connection c = getConnection()) {
            int res = Query.of("SELECT 1").as((rs, conn) -> {
                rs.next();
                return rs.getInt(1);
            }, c);
            if (res != 1) {
                throw new SQLException("SQL sanity check failed, expected 1 from SELECT 1; but got " + res + " instead.");
            }
            LOGGER.info("getConnection() succeeded and sanity check passed! {}", c.toString());
        } catch (Exception ex) {
            LOGGER.error("Could not get connection from pool! Cowardly stopping.", ex);
            System.exit(1);
        }
        LOGGER.info("Database loaded.");
    }

    public void migrate(boolean wait) throws Exception {
        LOGGER.info("!!!! RUNNING DATABASE MIGRATIONS !!!!");
        if (wait) {
            LOGGER.info("Waiting 5 seconds before continuing. If you did not mean to do this, exit now!");
            Thread.sleep(5000);
        }

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
