package io.banditoz.mchelper.database;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;
import io.avaje.config.Config;
import io.banditoz.mchelper.database.transaction.TransactionContext;
import io.banditoz.mchelper.di.annotations.RequiresDatabase;
import io.jenetics.facilejdbc.Query;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.hikaricp.v3_0.HikariTelemetry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Has-a class for managing a {@link HikariDataSource} pool for requesting database connections.<br>
 * Only Dao classes should interact with this class.
 */
@Singleton
@RequiresDatabase // inception?! nah, this annotation checks for existence of property needed to start DB... in theory
public class Database implements AutoCloseable {
    private final HikariDataSource pool;
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    @Inject
    public Database(OpenTelemetry openTelemetry) {
        pool = new HikariDataSource();
        pool.setJdbcUrl(Config.get("mchelper.database.url"));
        pool.setUsername(Config.get("mchelper.database.username"));
        pool.setPassword(Config.get("mchelper.database.password"));
        pool.setMaximumPoolSize(Config.getInt("mchelper.database.pool-size", 2));
        pool.setMetricsTrackerFactory(HikariTelemetry.create(openTelemetry).createMetricsTrackerFactory());

        try (Connection c = getConnection()) {
            String res = Query.of("SELECT VERSION();").as((rs, conn) -> {
                rs.next();
                return rs.getString(1);
            }, c);
            log.info("getConnection() succeeded. sqlVersion=\"{}\"", res);
        } catch (Exception ex) {
            log.error("Could not get connection from pool! Cowardly stopping.", ex);
            System.exit(1);
        }
        log.info("Database loaded.");
    }

    public void migrate(boolean wait) throws Exception {
        log.info("!!!! RUNNING DATABASE MIGRATIONS !!!!");
        if (wait) {
            log.info("Waiting 5 seconds before continuing. If you did not mean to do this, exit now!");
            Thread.sleep(5000);
        }

        try (Connection c = getConnection()) {
            liquibase.database.Database mgDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new Liquibase("sql/postgres/master.yml", new ClassLoaderResourceAccessor(), mgDb);
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    /**
     * @return A {@link Connection} to use, either one from the transaction, or a raw pool connection, if we're not in
     * a transaction.
     * @throws SQLException If a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        Connection tx = TransactionContext.current();
        return tx != null ? nonClosing(tx) : pool.getConnection();
    }

    /**
     * Gets a SQL connection to the pool.<br>
     * <b>Don't use this!</b> You will in 99% of cases want {@link Database#getConnection()} instead!
     *
     * @return A {@link Connection} to use, guaranteed not bound by any transaction.
     * @throws SQLException If a database access error occurs.
     */
    public Connection getRawConnection() throws SQLException {
        return pool.getConnection();
    }

    private Connection nonClosing(Connection c) {
        return (Connection) Proxy.newProxyInstance(
                c.getClass().getClassLoader(),
                new Class[]{Connection.class},
                (_, method, args) -> switch (method.getName()) {
                    case "close" -> null;
                    case "toString" -> "proxied transactional connection for " + method.invoke(c, args);
                    default -> method.invoke(c, args);
                }
        );
    }

    /**
     * Closes the underlying {@link HikariDataSource}
     */
    @Override
    public void close() {
        log.info("Closing {}...", this);
        pool.close();
    }
}
