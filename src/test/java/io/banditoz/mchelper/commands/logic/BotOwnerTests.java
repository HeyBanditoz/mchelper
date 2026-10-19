package io.banditoz.mchelper.commands.logic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.Test;

class BotOwnerTests {
    @Test
    void testBotOwners_happyPath() {
        User mockUser = mock(User.class);
        when(mockUser.getIdLong()).thenReturn(100L);
        assertThat(CommandPermissions.isBotOwner(mockUser)).isTrue();
        when(mockUser.getIdLong()).thenReturn(200L);
        assertThat(CommandPermissions.isBotOwner(mockUser)).isTrue();
        assertThat(CommandPermissions.isBotOwner(200L)).isTrue();
    }

    @Test
    void testBotOwners_negative() {
        User mockUser = mock(User.class);
        when(mockUser.getIdLong()).thenReturn(300L);
        assertThat(CommandPermissions.isBotOwner(mockUser)).isFalse();
        assertThat(CommandPermissions.isBotOwner(300L)).isFalse();
    }

    @Test
    void elevatedBoundCommandsAreOwnerOnly() {
        BoundCommand command = mock(BoundCommand.class, CALLS_REAL_METHODS);
        doReturn(true).when(command).isElevated();

        assertThat(command.canExecute(userWithId(100L))).isTrue();
        assertThat(command.canExecute(userWithId(300L))).isFalse();
    }

    @Test
    void boundCommandsAreOpenToEveryoneByDefault() {
        BoundCommand command = mock(BoundCommand.class, CALLS_REAL_METHODS);

        assertThat(command.canExecute(userWithId(300L))).isTrue();
    }

    private static User userWithId(long id) {
        User user = mock(User.class);
        when(user.getIdLong()).thenReturn(id);
        return user;
    }
}