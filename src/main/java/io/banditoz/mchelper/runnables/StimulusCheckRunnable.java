package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.money.AccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;

public class StimulusCheckRunnable implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(QotdRunnable.class);
    private final MCHelper MCHELPER;
    private final static BigDecimal ONE_THOUSAND = new BigDecimal("1000");

    public StimulusCheckRunnable(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
    }

    @Override
    public void run() {
        LOGGER.info("Processing stimulus check payments...");
        AccountManager am = MCHELPER.getAccountManager();
        try {
            int i = 0;
            for (long account : am.getAllAccounts()) {
                BigDecimal currentBal = am.queryBalance(account, false);
                if (currentBal.compareTo(new BigDecimal("1000")) < 0) {
                    BigDecimal diff = ONE_THOUSAND.subtract(currentBal);
                    am.add(diff, account, "stimulus check");
                    i++;
                    String oldBal = "$" + AccountManager.format(currentBal);
                    String newBal = "$" + AccountManager.format(diff);
                    Objects.requireNonNull(MCHELPER.getJDA().getUserById(account)).openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage("Hello. Since you have a balance of " + oldBal + ", your " +
                                    "account has been credited " + newBal + " to make $1,000.").queue(null, throwable ->
                                    LOGGER.warn("Failed to deliver DM regarding stimulus payments to " + account + ".", throwable)
                            ));
                }
            }
            LOGGER.info("Stimulus checks delivered to " + i + " user(s).");
        } catch (Exception ex) {
            LOGGER.error("Error while processing stimulus check payments!", ex);
        }
    }
}
