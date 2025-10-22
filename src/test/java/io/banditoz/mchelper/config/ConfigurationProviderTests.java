package io.banditoz.mchelper.config;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.commands.BaseCommandTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class ConfigurationProviderTests extends BaseCommandTest {
    @Inject
    ConfigurationProvider provider;

    @BeforeEach
    void clear() {
        truncate("guild_config");
    }

    @Test
    void testGetSetGet() throws SQLException {
        assertThat(provider.getValue(Config.PREFIX, ce.getGuild().getIdLong())).isEqualTo("!");
        provider.writeValue(Config.PREFIX, "%", ce.getGuild().getIdLong(), ce.getEvent().getAuthor().getIdLong());
        assertThat(provider.getValue(Config.PREFIX, ce.getGuild().getIdLong())).isEqualTo("%");
    }
}
