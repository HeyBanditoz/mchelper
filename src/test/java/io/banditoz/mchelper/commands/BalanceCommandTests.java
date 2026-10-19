package io.banditoz.mchelper.commands;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.money.MoneyException;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Member;
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
        Member member = mock(Member.class);
        when(member.getIdLong()).thenReturn(1234L);
        when(ce.getGuild().getMemberById(1234L)).thenReturn(member);
        setArgs("<@1234>");
        assertThatThrownBy(() -> bc.onCommand(ce)).isInstanceOf(MoneyException.class); // the mocked member has no account
    }
}
