package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Test(dependsOnGroups = {"DatabaseInitializationTests", "BalanceCommandTests"}, enabled = false) // disabled for april fools
public class WorkCommandTests extends BaseCommandTest {
    private final WorkCommand wc;

    public WorkCommandTests() {
        this.wc = new WorkCommand();
    }

    @Test
    public void testWorking() throws Exception {
        wc.onCommand(ce);
        assertThat(embedCaptor.getValue().getFooter().getText()).contains("New Balance:");
    }

    @Test(dependsOnMethods = {"testWorking"})
    public void testHasAWorkTransaction() throws Exception {
        AccountsDao dao = new AccountsDaoImpl(DB);
        long id = ce.getEvent().getAuthor().getIdLong();
        List<Transaction> txns = dao.getNTransactionsForUser(id, 100);
        Optional<Transaction> work = txns.stream().filter(transaction -> transaction.memo().contains("work")).findAny();
        assertThat(work).isNotEmpty();
        Transaction txn = work.get();
        assertThat(txn.to()).isEqualByComparingTo(id);
        assertThat(txn.from()).isNull();
        assertThat(txn.date()).isAfter(LocalDateTime.now().minusSeconds(60)); // should always pass
    }

    @Test(dependsOnMethods = {"testHasAWorkTransaction"})
    public void testCannotWorkAgain() throws Exception {
        wc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You cannot work until");
    }
}
