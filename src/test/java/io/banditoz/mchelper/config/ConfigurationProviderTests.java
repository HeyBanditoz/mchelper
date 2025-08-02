package io.banditoz.mchelper.config;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

import io.banditoz.mchelper.commands.BaseCommandTest;
import io.banditoz.mchelper.database.dao.GuildConfigDaoImpl;
import org.testng.annotations.Test;

@Test(dependsOnGroups = {"DatabaseInitializationTests"})
public class ConfigurationProviderTests extends BaseCommandTest {
    private final ConfigurationProvider provider;

    public ConfigurationProviderTests() {
        this.provider = new ConfigurationProvider(new GuildConfigDaoImpl(DB));
    }

    @Test
    public void testGetSetGet() throws SQLException {
        assertThat(provider.getValue(Config.PREFIX, ce.getGuild().getIdLong())).isEqualTo("!");
        provider.writeValue(Config.PREFIX, "%", ce.getGuild().getIdLong(), ce.getEvent().getAuthor().getIdLong());
        assertThat(provider.getValue(Config.PREFIX, ce.getGuild().getIdLong())).isEqualTo("%");
    }
}
