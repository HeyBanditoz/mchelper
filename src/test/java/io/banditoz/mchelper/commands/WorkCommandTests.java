package io.banditoz.mchelper.commands;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import io.banditoz.mchelper.ObjectMapperFactory;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.database.dao.AccountsDaoImpl;
import io.banditoz.mchelper.database.dao.TasksDaoImpl;
import io.banditoz.mchelper.money.AccountManager;
import org.testng.annotations.Test;

@Test(dependsOnGroups = {"DatabaseInitializationTests", "BalanceCommandTests"})
public class WorkCommandTests extends BaseCommandTest {
    private final WorkCommand wc;

    public WorkCommandTests() {
        this.wc = new WorkCommand(
                new ObjectMapperFactory().objectMapper(),
                new AccountManager(new AccountsDaoImpl(DB)),
                new TasksDaoImpl(DB)
        );
        wc.populateResponses();
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
