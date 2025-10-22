package io.banditoz.mchelper.commands;

import io.avaje.inject.test.InjectTest;
import io.banditoz.mchelper.Mocks;
import io.banditoz.mchelper.database.Lottery;
import io.banditoz.mchelper.database.LotteryEntrant;
import io.banditoz.mchelper.database.Transaction;
import io.banditoz.mchelper.database.dao.AccountsDao;
import io.banditoz.mchelper.database.dao.LotteryDao;
import io.banditoz.mchelper.money.AccountManager;
import io.banditoz.mchelper.money.lottery.LotteryManager;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@InjectTest
class LotteryCommandTests extends BaseCommandTest {
    @Inject
    LotteryCommand lc;
    @Inject
    AccountsDao aDao;
    @Inject
    AccountManager am;
    @Inject
    LotteryDao lDao;
    @Spy
    LotteryManager lm;
    @Inject
    JDA jda;

    @BeforeEach
    void clear() {
        truncate("accounts", "transactions", "lottery", "lottery_entrants");
        resetSequence("lottery_id_seq");
    }

    @Test
    void lotteryCommand_happyPath() throws Exception {
        am.queryBalance(Mocks.getMockedMember().getIdLong(), true);
        am.queryBalance(Mocks.getDifferentMockedMember().getIdLong(), true);
        am.queryBalance(Mocks.getDiffDiffMockedMember().getIdLong(), true);

        Member m = ce.getEvent().getMember();
        Guild g = Mocks.getMockedGuild();
        when(m.getGuild()).thenReturn(g);
        when(ce.getEvent().getMember()).thenReturn(m);

        setArgs("500");
        lc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You have entered the lottery.", "$500, 100.00%");
        List<Transaction> txns = aDao.getNTransactionsForUser(ce.getEvent().getMember().getIdLong(), 100);
        assertThat(txns.stream()
                .filter(transaction -> transaction.from() != null)
                .filter(transaction -> transaction.from() == ce.getEvent().getMember().getIdLong())
                .map(Transaction::memo))
                .contains("lottery ticket for 1");
        verify(lm, times(0)).scheduleCountdown(any());

        resetMocks();
        m = Mocks.getDifferentMockedMember();
        g = Mocks.getMockedGuild();
        when(m.getGuild()).thenReturn(g);
        when(ce.getEvent().getMember()).thenReturn(m);

        setArgs("250");
        lc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You have entered the lottery.", "250, 33.33%");
        txns = aDao.getNTransactionsForUser(ce.getEvent().getMember().getIdLong(), 100);
        assertThat(txns.stream()
                .filter(transaction -> transaction.from() != null)
                .filter(transaction -> transaction.from() == ce.getEvent().getMember().getIdLong())
                .map(Transaction::memo))
                .contains("lottery ticket for 1");
        // second user entered, should schedule now
        verify(lm, times(1)).scheduleCountdown(any());

        resetMocks();
        m = Mocks.getDiffDiffMockedMember();
        g = Mocks.getMockedGuild();
        when(m.getGuild()).thenReturn(g);
        when(ce.getEvent().getMember()).thenReturn(m);

        // give that user money from initial account creation
        am.queryBalance(m.getIdLong(), true);
        setArgs("250");
        lc.onCommand(ce);
        assertThat(stringCaptor.getValue()).contains("You have entered the lottery.", "250, 25.00%");
        txns = aDao.getNTransactionsForUser(ce.getEvent().getMember().getIdLong(), 100);
        assertThat(txns.stream()
                .filter(transaction -> transaction.from() != null)
                .filter(transaction -> transaction.from() == ce.getEvent().getMember().getIdLong())
                .map(Transaction::memo))
                .contains("lottery ticket for 1");
        // third user entered, don't schedule countdown again
        verify(lm, times(1)).scheduleCountdown(any());

        List<LotteryEntrant> entrants = lm.getEntrantsForLottery(ce.getGuild());
        assertThat(entrants.stream().map(LotteryEntrant::userId)).containsExactlyInAnyOrder(
                163094867910590464L, 404837963697225729L, 537893458730429519L
        );
        assertThat(entrants.stream().map(LotteryEntrant::amount)).containsExactlyInAnyOrder(
                new BigDecimal("250.00"), new BigDecimal("250.00"), new BigDecimal("500.00")
        );

        Method payout = LotteryManager.class.getDeclaredMethod("payout", Lottery.class);
        payout.setAccessible(true);
        Lottery l = lm.getLottery(ce.getGuild());
        assertThat(l.complete()).isFalse();
        assertThat(l.guildId()).isEqualTo(ce.getGuild().getIdLong());

        when(jda.getGuildById(g.getIdLong())).thenReturn(g);
        payout.invoke(lm, l);
        assertThat(lm.getLottery(ce.getGuild())).isNull();
        // TODO finish these
//        List<Transaction> allTxns = new ArrayList<>();
//        allTxns.addAll(aDao.getNTransactionsForUser(Mocks.getMockedMember().getIdLong(), 1000));
//        allTxns.addAll(aDao.getNTransactionsForUser(Mocks.getDifferentMockedMember().getIdLong(), 1000));
//        List<Transaction> lotteryWinningTransactions = allTxns.stream()
//                .filter(transaction -> transaction.to() != null)
//                .filter(transaction -> transaction.memo().contains("lottery winnings"))
//                .toList();
//        assertThat(lotteryWinningTransactions.stream().filter(transaction -> transaction.memo().contains("lottery winnings for")).map(Transaction::to)).containsAnyOf(163094867910590464L, 404837963697225729L);
    }
}
