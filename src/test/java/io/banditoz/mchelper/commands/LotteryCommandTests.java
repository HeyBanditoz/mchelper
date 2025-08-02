package io.banditoz.mchelper.commands;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.database.Lottery;
import io.banditoz.mchelper.database.LotteryEntrant;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.database.dao.AccountsDaoImpl;
import io.banditoz.mchelper.database.dao.LotteryDao;
import io.banditoz.mchelper.database.dao.LotteryDaoImpl;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(dependsOnGroups = {"DatabaseInitializationTests", "BalanceCommandTests", "TransferCommandTests"})
public class LotteryCommandTests extends BaseCommandTest {
    private LotteryCommand lc;
    private AccountsDao aDao = new AccountsDaoImpl(DB);
    private AccountManager am = new AccountManager(aDao);
    private LotteryDao lDao = new LotteryDaoImpl(DB);
    private LotteryManager lm;

    @BeforeClass
    public void setup() {
        lm = spy(new LotteryManager(
                lDao,
                new AccountManager(aDao),
                mock(ScheduledExecutorService.class),
                mock(JDA.class)
        ));
        this.lc = new LotteryCommand(am, lm);
    }

    @Test
    public void testUserOneCreatingAndEnteringLottery() throws Exception {
        AccountsDao dao = new AccountsDaoImpl(DB);
        Member m = ce.getEvent().getMember();
        Guild g = Mocks.getMockedGuild();
        when(m.getGuild()).thenReturn(g);
        when(ce.getEvent().getMember()).thenReturn(m);

        setArgs("500");
        lc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You have entered the lottery.", "$500, 100.00%");
        List<Transaction> txns = dao.getNTransactionsForUser(ce.getEvent().getMember().getIdLong(), 100);
        assertThat(txns.stream()
                .filter(transaction -> transaction.from() != null)
                .filter(transaction -> transaction.from() == ce.getEvent().getMember().getIdLong())
                .map(Transaction::memo))
                .contains("lottery ticket for 1");
        verify(lm, times(0)).scheduleCountdown(any());
    }

    @Test(dependsOnMethods = {"testUserOneCreatingAndEnteringLottery"})
    public void testAnotherUserEnteringLottery() throws Exception {
        Member m = Mocks.getDifferentMockedMember();
        Guild g = Mocks.getMockedGuild();
        when(m.getGuild()).thenReturn(g);
        when(ce.getEvent().getMember()).thenReturn(m);

        setArgs("250");
        lc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You have entered the lottery.", "250, 33.33%");
        List<Transaction> txns = aDao.getNTransactionsForUser(ce.getEvent().getMember().getIdLong(), 100);
        assertThat(txns.stream()
                .filter(transaction -> transaction.from() != null)
                .filter(transaction -> transaction.from() == ce.getEvent().getMember().getIdLong())
                .map(Transaction::memo))
                .contains("lottery ticket for 1");
        // second user entered, should schedule now
        verify(lm, times(1)).scheduleCountdown(any());
    }

    @Test(dependsOnMethods = {"testAnotherUserEnteringLottery"})
    public void testThirdUserEnteringLottery() throws Exception {
        Member m = Mocks.getDiffDiffMockedMember();
        Guild g = Mocks.getMockedGuild();
        when(m.getGuild()).thenReturn(g);
        when(ce.getEvent().getMember()).thenReturn(m);

        // give that user money from initial account creation
        am.queryBalance(m.getIdLong(), true);
        setArgs("250");
        lc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You have entered the lottery.", "250, 25.00%");
        List<Transaction> txns = aDao.getNTransactionsForUser(ce.getEvent().getMember().getIdLong(), 100);
        assertThat(txns.stream()
                .filter(transaction -> transaction.from() != null)
                .filter(transaction -> transaction.from() == ce.getEvent().getMember().getIdLong())
                .map(Transaction::memo))
                .contains("lottery ticket for 1");
        // third user entered, don't schedule countdown again
        verify(lm, times(1)).scheduleCountdown(any());
    }

    @Test(dependsOnMethods = {"testAnotherUserEnteringLottery"})
    public void testUsersInEntrantsTable() throws Exception {
        List<LotteryEntrant> entrants = lm.getEntrantsForLottery(ce.getGuild());
        assertThat(entrants.stream().map(LotteryEntrant::userId)).containsExactlyInAnyOrder(
                163094867910590464L, 404837963697225729L, 537893458730429519L
        );
        assertThat(entrants.stream().map(LotteryEntrant::amount)).containsExactlyInAnyOrder(
                new BigDecimal("250.00"), new BigDecimal("250.00"), new BigDecimal("500.00")
        );
    }

    @Test(dependsOnMethods = {"testUsersInEntrantsTable"})
    public void testWin() throws Exception {
        Method payout = LotteryManager.class.getDeclaredMethod("payout", Lottery.class);
        payout.setAccessible(true);
        Lottery l = lm.getLottery(ce.getGuild());
        assertThat(l.complete()).isFalse();
        assertThat(l.guildId()).isEqualTo(ce.getGuild().getIdLong());

        payout.invoke(lm, l);
        // TODO finish these
//        l = ce.getMCHelper().getLotteryManager().getLottery(ce.getGuild());
//        assertThat(l.complete()).isTrue();
//        ArrayList<Transaction> txns = new ArrayList<>();
//        txns.addAll(dao.getNTransactionsForUser(Mocks.getMockedMember().getIdLong(), 1000));
//        txns.addAll(dao.getNTransactionsForUser(Mocks.getDifferentMockedMember().getIdLong(), 1000));
//        List<Transaction> lotteryWinningTransactions = txns.stream()
//                .filter(transaction -> transaction.to() != null)
//                .filter(transaction -> transaction.memo().contains("lottery winnings"))
//                .toList();
//        assertThat(lotteryWinningTransactions.stream().filter(transaction -> transaction.memo().contains("lottery winnings for")).map(Transaction::to)).containsAnyOf(163094867910590464L, 404837963697225729L);
    }
}
