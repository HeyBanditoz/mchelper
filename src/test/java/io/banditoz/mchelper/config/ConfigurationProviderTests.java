package io.banditoz.mchelper.config;

import io.banditoz.mchelper.commands.BaseCommandTest;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Test(dependsOnGroups = {"DatabaseInitializationTests"})
public class ConfigurationProviderTests extends BaseCommandTest {
    private final ConfigurationProvider provider;

    public ConfigurationProviderTests() {
        this.provider = new ConfigurationProvider(DB);
    }

    @Test
    public void testGetSetGet() throws SQLException {
        assertThat(provider.getValue(Config.PREFIX, ce.getGuild().getIdLong())).isEqualTo("!");
        provider.writeValue(Config.PREFIX, "%", ce.getGuild().getIdLong(), ce.getEvent().getAuthor().getIdLong());
        assertThat(provider.getValue(Config.PREFIX, ce.getGuild().getIdLong())).isEqualTo("%");
    }
}
