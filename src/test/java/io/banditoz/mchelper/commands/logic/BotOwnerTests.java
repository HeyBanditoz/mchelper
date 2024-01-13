package io.banditoz.mchelper.commands.logic;

import net.dv8tion.jda.api.entities.User;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BotOwnerTests {
    @Test
    public void testBotOwners_happyPath() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("100");
        assertThat(CommandPermissions.isBotOwner(mockUser)).isTrue();
        when(mockUser.getId()).thenReturn("200");
        assertThat(CommandPermissions.isBotOwner(mockUser)).isTrue();
    }

    @Test
    public void testBotOwners_negative() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("300");
        assertThat(CommandPermissions.isBotOwner(mockUser)).isFalse();
    }
}