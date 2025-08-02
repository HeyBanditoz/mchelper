package io.banditoz.mchelper.di;

import java.util.Objects;
import java.util.Optional;

import io.avaje.config.Config;
import io.avaje.inject.spi.ConfigPropertyPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A ConfigPropertyPlugin based on logic in {@link io.avaje.inject.DSystemProps} that falls back to
 * {@link io.avaje.inject.DConfigProps} if needed.
 */
public class ConfigPlugin implements ConfigPropertyPlugin {
    @NotNull
    @Override
    public Optional<String> get(@NotNull String property) {
        String prop = System.getenv().get(property);
        if (prop == null) {
            return Config.getOptional(property);
        }
        return Optional.empty();
    }

    @Override
    public boolean contains(@NotNull String property) {
        return System.getenv().containsKey(property) || Config.getNullable(property) != null;
    }

    @Override
    public boolean equalTo(@NotNull String property, @NotNull String value) {
        String prop = System.getenv().get(property);
        if (!Objects.equals(prop, value)) {
            return value.equals(Config.getNullable(property));
        }
        return true;
    }
}
