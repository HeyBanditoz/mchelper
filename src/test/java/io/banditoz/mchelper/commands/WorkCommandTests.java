package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.utils.database.Transaction;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test(dependsOnGroups = {"DatabaseInitializationTests", "BalanceCommandTests"})
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
        assertThat(dao.getNTransactionsForUser(id, 100)).map(Transaction::getMemo).containsAnyOf("daily work");
    }

    @Test(dependsOnMethods = {"testHasAWorkTransaction"})
    public void testCannotWorkAgain() throws Exception {
        wc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You cannot work until");
    }
}
