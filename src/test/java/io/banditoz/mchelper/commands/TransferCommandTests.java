package io.banditoz.mchelper.commands;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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

    /** The recipient now arrives as a bound parameter, so it is resolved from the mention in the message. */
    private static final String MENTION = "<@163094867910590464>";

    @BeforeEach
    void init() {
        truncate("accounts", "transactions");
        // Resolved before stubbing starts; calling a mock inside thenReturn() leaves the stubbing unfinished.
        User user = member.getUser();
        when(ce.getEvent().getJDA().getUserById(member.getIdLong())).thenReturn(user);
    }

    @Test
    void testTransferCommand() throws Exception {
        am.queryBalance(member.getIdLong(), true);
        am.queryBalance(otherUser.getIdLong(), true);
        when(ce.getUser()).thenReturn(otherUser);
        setArgs(MENTION + " 400");
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

        setArgs(MENTION + " -400");
        assertThatThrownBy(() -> tc.onCommand(ce)).isInstanceOf(MoneyException.class); // can't transfer negative amount
    }

    @Test
    void testCannotTransferToSelf() throws Exception {
        am.queryBalance(member.getIdLong(), true);
        am.queryBalance(otherUser.getIdLong(), true);

        // ce.getUser() is the mocked member, and the mention resolves to the same person
        setArgs(MENTION + " 400");
        assertThatThrownBy(() -> tc.onCommand(ce)).isInstanceOf(MoneyException.class); // can't transfer to self
    }

    @Test
    void amountKeepsItsPrecision() throws Exception {
        am.queryBalance(member.getIdLong(), true);
        am.queryBalance(otherUser.getIdLong(), true);
        when(ce.getUser()).thenReturn(otherUser);
        setArgs(MENTION + " 0.10");
        tc.onCommand(ce);

        Optional<Transaction> transfer = dao.getNTransactionsForUser(member.getIdLong(), 100).stream()
                .filter(transaction -> transaction.memo().contains("transfer"))
                .findAny();
        assertThat(transfer).isNotEmpty();
        assertThat(transfer.get().amount()).isEqualByComparingTo(new BigDecimal("-0.10"));
    }
}
