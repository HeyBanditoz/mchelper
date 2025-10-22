package io.banditoz.mchelper.config;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;
import io.banditoz.mchelper.database.Database;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.inject.Named;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.Nullable;

@TestScope
@Factory
public class DatabaseConfig {
    private final GenericContainer<?> postgresContainer;

    public DatabaseConfig(@Named("postgresContainer") @Nullable GenericContainer<?> postgresContainer) {
        this.postgresContainer = postgresContainer;
    }

    @Bean
    public Database database() throws Exception {
        // postgresContainer will be null if the `NO_DOCKER_POSTGRES` env var is set.
        // If that's the case, we'll just use the pre-configured postgres database
        if (postgresContainer != null) {
            Config.setProperty("mchelper.database.url", "jdbc:postgresql://%s:%d/postgres/"
                    .formatted(postgresContainer.getHost(), postgresContainer.getMappedPort(5432)));
            Config.setProperty("mchelper.database.username", "postgres");
            Config.setProperty("mchelper.database.password", "postgres");
        }
        Database database = new Database(OpenTelemetry.noop());
        database.migrate(false);
        return database;
    }

}
