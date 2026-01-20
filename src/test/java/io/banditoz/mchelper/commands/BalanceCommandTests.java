package io.banditoz.mchelper.commands;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.money.MoneyException;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@InjectTest
class BalanceCommandTests extends BaseCommandTest {
    @Inject
    BalanceCommand bc;

    @BeforeEach
    void clear() {
        truncate("accounts", "transactions");
    }

    @Test
    void testAccountCreation() throws Exception {
        bc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Your balance: $1,000");
    }

    @Test
    void testInvalidUser() {
        User user = mock(User.class);
        when(user.getIdLong()).thenReturn(1234L);
        when(ce.getMentionedUsers()).thenReturn(List.of(user));
        when(ce.isFromGuild()).thenReturn(true);
        when(ce.getGuild().getMemberById(anyLong())).thenReturn(mock(Member.class));
        assertThatThrownBy(() -> bc.onCommand(ce)).isInstanceOf(MoneyException.class); // the other mocked member has no account
    }
}
