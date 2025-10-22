package io.banditoz.mchelper.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;
import io.avaje.inject.test.TestScope;
import jakarta.inject.Named;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nullable;

@TestScope
@Factory
public class PostgresTestContainerConfig {
    @Bean
    @Named("postgresContainer")
    @RequiresProperty(missing = "NO_DOCKER_POSTGRES")
    @Nullable
    public GenericContainer<?> postgresTestContainer() {
        return (GenericContainer<?>) new GenericContainer(DockerImageName.parse("postgres:18-alpine")).withExposedPorts(5432);
    }
}
