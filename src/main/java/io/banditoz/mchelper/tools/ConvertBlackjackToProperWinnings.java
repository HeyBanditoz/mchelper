package io.banditoz.mchelper.tools;

import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.Transaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConvertBlackjackToProperWinnings {
    private final static String ANTE = "blackjack ante";
    private final static String WINNINGS = "blackjack winnings";

    private static final String STANDOFF = "blackjack standoff";
    private static final String TONE = "blackjack 21 winnings";

    private static final BigDecimal ONE = new BigDecimal("1.00");
    private static final BigDecimal ONE_HALF = new BigDecimal("0.50");

    private static int count = 0;

    public static void main(String[] args) {
        Settings s = new SettingsManager(new File(".").toPath().resolve("Config.json")).getSettings();
        Database db = new Database(s);

        List<Transaction> txns = new ArrayList<>(); // hacky
        try (Connection c = db.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE memo LIKE '%blackjack%';");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                txns.add(Transaction.of(rs));
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        txns.sort(Transaction::compareTo);
        System.out.println("Transactions size: " + txns.size());

        try (Connection c = db.getConnection()) {
            for (int i = 0; i < txns.size(); i++) {
                Transaction original = txns.get(i);
                if (original.getMemo().equals(WINNINGS)) {
                    // iterate backwards until we find the transactions that the user started with
                    for (int j = i - 1; j > 0; j--) {
                        Transaction candidate = txns.get(j);
                        BigDecimal candidateAmount = new BigDecimal(String.valueOf(candidate.getAmount())).setScale(2, RoundingMode.HALF_UP).negate(); // so hacky!

                        if (candidate.getMemo().equals(ANTE) && candidate.getFrom().equals(original.getTo())) {
                            // these if statement chains suck, but I am lazy and hungry
                            if (candidateAmount.divide(original.getAmount(), RoundingMode.HALF_UP).equals(ONE)) {
                                updateToRightThing(c, original, false);
                            }
                            else if (candidateAmount.divide(original.getAmount(), RoundingMode.HALF_UP).equals(ONE_HALF)) {
                            }
                            else {
                                updateToRightThing(c, original, true);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println(count);
        System.exit(0);
    }

    private static void updateToRightThing(Connection c, Transaction txn, boolean isSuperWin) throws SQLException {
        PreparedStatement ps = c.prepareStatement("UPDATE transactions SET memo = ? WHERE `when` = ? AND `to_id` = ? AND `memo` LIKE '%blackjack winnings%'");
        ps.setLong(3, txn.getTo());
        if (isSuperWin) {
            ps.setString(1, TONE);
        }
        else {
            ps.setString(1, STANDOFF);
        }
        ps.setTimestamp(2, Timestamp.valueOf(txn.getDate()));
        ps.execute();
        count++;
        ps.close();
    }
}
