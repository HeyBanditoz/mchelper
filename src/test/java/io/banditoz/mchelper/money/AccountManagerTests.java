package io.banditoz.mchelper.money;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.BaseTest;
import io.banditoz.mchelper.database.StatPoint;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.Type;
import io.banditoz.mchelper.database.dao.AccountsDao;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@InjectTest
class AccountManagerTests extends BaseTest {
    @Inject
    AccountManager accountManager;
    @Inject
    AccountsDao accountsDao;

    @BeforeEach
    void clear() {
        truncate("accounts", "transactions");
    }

    @Test
    void createAccount_happy() throws Exception {
        BigDecimal oneThousand = new BigDecimal("1000");
        assertThat(accountManager.queryBalance(1, true)).isEqualTo(oneThousand);

        StatPoint<Long> balance = accountManager.getAllBalances().getFirst();
        assertThat(balance.getThing()).isEqualTo(1);
        assertThat(balance.getCount().doubleValue()).isEqualTo(1000);

        List<Transaction> txns = accountsDao.getAllTransactions();
        assertThat(txns).hasSize(1);
        Transaction txn = txns.get(0);

        assertThat(txn.to()).isEqualTo(1);
        assertThat(txn.from()).isNull();
        assertThat(txn.getFinalAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(txn.memo()).isEqualTo("seed money");
        assertThat(txn.date()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
    }

    @Test
    void transfer_happy() throws Exception {
        accountManager.queryBalance(1, true);
        accountManager.queryBalance(2, true);

        accountManager.transferTo(new BigDecimal("500"), 1, 2, "transfer!");
        assertThat(accountManager.queryBalance(1, false).doubleValue()).isEqualTo(500);
        assertThat(accountManager.queryBalance(2, false).doubleValue()).isEqualTo(1500);

        List<Transaction> txns = accountsDao.getAllTransactions();
        assertThat(txns).hasSize(3);
        Transaction transferTxn = txns.stream().filter(txn -> txn.type() == Type.TRANSFER).findFirst().orElseThrow();

        assertThat(transferTxn.to()).isEqualTo(2);
        assertThat(transferTxn.from()).isEqualTo(1);
        assertThat(transferTxn.amount()).isEqualTo(new BigDecimal("-500.00"));
        // finalAmount here is the transferer's final amount, rather the transferee's
        // this unfortunately means you cannot compute accurate balance history for a particular user without walking the
        // transaction list to find a non-transfer transaction (there will be at least one, the seed money transaction)
        // this may change in the future :p
        assertThat(transferTxn.getFinalAmount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(transferTxn.memo()).isEqualTo("transfer!");
        assertThat(transferTxn.date()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
    }

    @Test
    void cannotTransferZeroOrLessThan() throws Exception {
        accountManager.queryBalance(1, true);
        accountManager.queryBalance(2, true);

        assertThatThrownBy(() -> accountManager.transferTo(new BigDecimal("0"), 1, 2, "memo"))
                .isInstanceOf(MoneyException.class)
                .hasMessage("must change more than zero!");
        assertThatThrownBy(() -> accountManager.transferTo(new BigDecimal("-100"), 1, 2, "memo"))
                .isInstanceOf(MoneyException.class)
                .hasMessage("must change more than zero!");

        assertThat(accountManager.queryBalance(1, false).doubleValue()).isEqualTo(1000);
        assertThat(accountManager.queryBalance(2, false).doubleValue()).isEqualTo(1000);
    }
}
