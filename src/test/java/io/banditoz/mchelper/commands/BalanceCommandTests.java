package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.money.MoneyException;
import io.banditoz.mchelper.utils.database.dao.AccountsDao;
import io.banditoz.mchelper.utils.database.dao.AccountsDaoImpl;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(dependsOnGroups = {"DatabaseInitializationTests"}, groups = "BalanceCommandTests")
public class BalanceCommandTests extends BaseCommandTest {
    private final BalanceCommand bc;

    public BalanceCommandTests() {
        this.bc = new BalanceCommand();
    }

    @Test
    public void testAccountCreation() throws Exception {
        bc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Your balance: $1,000");
    }

    @Test(dependsOnMethods = {"testAccountCreation"})
    public void testAnotherAccountCreation() throws Exception {
        User u = Mocks.getDifferentMockedMember().getUser();
        when(ce.getEvent().getAuthor()).thenReturn(u);
        bc.onCommand(ce);
        assertThat(stringCaptor.getValue()).isEqualTo("Your balance: $1,000");
    }

    @Test(dependsOnMethods = {"testAccountCreation"})
    public void testHasAccountAndInitialTransaction() throws Exception {
        AccountsDao dao = new AccountsDaoImpl(DB);
        long id = ce.getEvent().getAuthor().getIdLong();
        long id2 = Mocks.getMockedMember().getIdLong();
        assertThat(dao.getAllAccounts()).containsExactlyInAnyOrder(id, id2);
        assertThat(dao.getNTransactionsForUser(id, 100)).hasSize(1);
        assertThat(dao.getNTransactionsForUser(id2, 100)).hasSize(1);
    }

    @Test(dependsOnMethods = {"testAccountCreation", "testAnotherAccountCreation"})
    public void testInvalidUser() {
        Member m = mock(Member.class);
        when(m.getIdLong()).thenReturn(1234L);
        when(ce.getMentionedMembers()).thenReturn(List.of(m));
        assertThatThrownBy(() -> bc.onCommand(ce)).isInstanceOf(MoneyException.class); // the other mocked member has no account
    }
}
