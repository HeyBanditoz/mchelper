package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.stats.Status;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class WorkCommandTests extends BaseCommandTest {
    @Inject
    WorkCommand wc;
    @Inject
    AccountManager am;
    @Inject
    AccountsDao dao;

    @BeforeEach
    void clear() {
        truncate("accounts", "transactions", "tasks");
    }

    @Test
    void testWorking() throws Exception {
        am.queryBalance(ce.getUser().getIdLong(), true); // prime the account
        wc.onCommand(ce);
        assertThat(embedCaptor.getValue().getFooter().getText()).contains("New Balance:");

        long id = ce.getEvent().getAuthor().getIdLong();
        List<Transaction> txns = dao.getNTransactionsForUser(id, 100);
        Optional<Transaction> work = txns.stream().filter(transaction -> transaction.memo().contains("work")).findAny();
        assertThat(work).isNotEmpty();
        Transaction txn = work.get();
        assertThat(txn.to()).isEqualByComparingTo(id);
        assertThat(txn.from()).isNull();
        assertThat(txn.date()).isAfter(LocalDateTime.now().minusSeconds(60)); // should always pass
    }

    @Test
    public void testCannotWorkAgain() throws Exception {
        am.queryBalance(ce.getUser().getIdLong(), true); // prime the account
        Status status = wc.onCommand(ce);
        assertThat(status).isEqualTo(Status.SUCCESS);
        status = wc.onCommand(ce);
        assertThat(status).isEqualTo(Status.COOLDOWN);
        assertThat(stringCaptor.getValue()).contains("You cannot work until");
    }
}
