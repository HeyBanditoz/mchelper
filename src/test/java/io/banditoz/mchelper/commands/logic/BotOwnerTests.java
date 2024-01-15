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
        when(mockUser.getIdLong()).thenReturn(100L);
        assertThat(CommandPermissions.isBotOwner(mockUser)).isTrue();
        when(mockUser.getIdLong()).thenReturn(200L);
        assertThat(CommandPermissions.isBotOwner(mockUser)).isTrue();
        assertThat(CommandPermissions.isBotOwner(200L)).isTrue();
    }

    @Test
    public void testBotOwners_negative() {
        User mockUser = mock(User.class);
        when(mockUser.getIdLong()).thenReturn(300L);
        assertThat(CommandPermissions.isBotOwner(mockUser)).isFalse();
        assertThat(CommandPermissions.isBotOwner(300L)).isFalse();
    }
}