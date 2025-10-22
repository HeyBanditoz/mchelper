package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.MoneyException;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@InjectTest
class TransferCommandTests extends BaseCommandTest {
    @Inject
    TransferCommand tc;
    @Inject
    AccountManager am;
    @Inject
    AccountsDao dao;

    private final Member member = Mocks.getMockedMember();
    private final User otherUser = Mocks.getDifferentMockedMember().getUser();

    @BeforeEach
    void init() {
        truncate("accounts", "transactions");
        when(ce.getMentionedMembers()).thenReturn(List.of(member));
        when(ce.getRawCommandArgs()).thenReturn(new String[]{"!transfer", "", "400"});
    }

    @Test
    void testTransferCommand() throws Exception {
        am.queryBalance(member.getIdLong(), true);
        am.queryBalance(otherUser.getIdLong(), true);
        when(ce.getEvent().getAuthor()).thenReturn(otherUser);
        when(ce.getMentionedMembers()).thenReturn(List.of(member));
        tc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Transfer of $400 to <@!163094867910590464> complete. You have $600 left.");

        Optional<Transaction> transfer = dao.getNTransactionsForUser(member.getIdLong(), 100).stream().filter(transaction -> transaction.memo().contains("transfer")).findAny();

        // assertions on the transactions
        assertThat(transfer).isNotEmpty();
        Transaction t = transfer.get();
        assertThat(t.from()).isEqualByComparingTo(otherUser.getIdLong());
        assertThat(t.to()).isEqualByComparingTo(member.getIdLong());
        assertThat(t.amount()).isEqualByComparingTo(new BigDecimal("-400.00"));
        assertThat(t.memo()).isEqualTo("transfer");

        // make sure balances of the test accounts are good too
        assertThat(am.queryBalance(otherUser.getIdLong(), false)).isEqualByComparingTo(new BigDecimal("600"));
        assertThat(am.queryBalance(member.getIdLong(), false)).isGreaterThanOrEqualTo(new BigDecimal("1400"));
    }

    @Test
    void cannotTransferNegativeAmount() throws Exception {
        am.queryBalance(member.getIdLong(), true);
        am.queryBalance(otherUser.getIdLong(), true);

        setArgs("a -400");
        assertThatThrownBy(() -> tc.onCommand(ce)).isInstanceOf(MoneyException.class); // can't transfer negative amount
    }

    @Test
    void testCannotTransferToSelf() throws Exception {
        am.queryBalance(member.getIdLong(), true);
        am.queryBalance(otherUser.getIdLong(), true);

        setArgs("a 400");
        User u = member.getUser();
        when(ce.getEvent().getAuthor()).thenReturn(u);
        when(ce.getMentionedMembers()).thenReturn(List.of(member));
        assertThatThrownBy(() -> tc.onCommand(ce)).isInstanceOf(MoneyException.class); // can't transfer to self
    }
}
