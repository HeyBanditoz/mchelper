package io.banditoz.mchelper;

import io.banditoz.mchelper.money.AccountManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountManagerFormattingTests {
    @Test
    public void testOneZeroZero() {
        BigDecimal bd = new BigDecimal("1.00");
        assertEquals("1", AccountManager.format(bd));
    }

    @Test
    public void testOne() {
        BigDecimal bd = new BigDecimal("1");
        assertEquals("1", AccountManager.format(bd));
    }

    @Test
    public void testOneOneZero() {
        BigDecimal bd = new BigDecimal("1.10");
        assertEquals("1.10", AccountManager.format(bd));
    }

    @Test
    public void testOneZeroOne() {
        BigDecimal bd = new BigDecimal("1.01");
        assertEquals("1.01", AccountManager.format(bd));
    }

    @Test
    public void testZeroZeroOne() {
        BigDecimal bd = new BigDecimal("0.01");
        assertEquals("0.01", AccountManager.format(bd));
    }

    @Test
    public void testZeroOneZero() {
        BigDecimal bd = new BigDecimal("0.10");
        assertEquals("0.10", AccountManager.format(bd));
    }

    @Test
    public void testLargeNumber() {
        BigDecimal bd = new BigDecimal("1234.56");
        assertEquals("1,234.56", AccountManager.format(bd));
    }

    @Test
    public void testLargeNumberTwo() {
        BigDecimal bd = new BigDecimal("1234.5");
        assertEquals("1,234.50", AccountManager.format(bd));
    }

    @Test
    public void testLargeNumberThree() {
        BigDecimal bd = new BigDecimal("1234");
        assertEquals("1,234", AccountManager.format(bd));
    }
}
