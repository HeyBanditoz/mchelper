package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Lottery;
import io.banditoz.mchelper.utils.database.LotteryEntrant;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface LotteryDao {
    Lottery getActiveLottery(Guild g) throws SQLException;
    List<Lottery> getAllActiveLotteries() throws SQLException;
    void createLottery(TextChannel c, BigDecimal max) throws SQLException;
    void enterLottery(Member m, BigDecimal amount) throws SQLException;
    List<LotteryEntrant> getEntrantsForLottery(Guild g) throws SQLException;
    void markLotteryComplete(long lotteryId) throws SQLException;
}
