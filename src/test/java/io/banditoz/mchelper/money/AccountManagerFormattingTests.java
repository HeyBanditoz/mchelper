package io.banditoz.mchelper.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountManagerFormattingTests {
    @Test
    void testOneZeroZero() {
        BigDecimal bd = new BigDecimal("1.00");
        assertEquals("1", AccountManager.format(bd));
    }

    @Test
    void testOne() {
        BigDecimal bd = new BigDecimal("1");
        assertEquals("1", AccountManager.format(bd));
    }

    @Test
    void testOneOneZero() {
        BigDecimal bd = new BigDecimal("1.10");
        assertEquals("1.10", AccountManager.format(bd));
    }

    @Test
    void testOneZeroOne() {
        BigDecimal bd = new BigDecimal("1.01");
        assertEquals("1.01", AccountManager.format(bd));
    }

    @Test
    void testZeroZeroOne() {
        BigDecimal bd = new BigDecimal("0.01");
        assertEquals("0.01", AccountManager.format(bd));
    }

    @Test
    void testZeroOneZero() {
        BigDecimal bd = new BigDecimal("0.10");
        assertEquals("0.10", AccountManager.format(bd));
    }

    @Test
    void testLargeNumber() {
        BigDecimal bd = new BigDecimal("1234.56");
        assertEquals("1,234.56", AccountManager.format(bd));
    }

    @Test
    void testLargeNumberTwo() {
        BigDecimal bd = new BigDecimal("1234.5");
        assertEquals("1,234.50", AccountManager.format(bd));
    }

    @Test
    void testLargeNumberThree() {
        BigDecimal bd = new BigDecimal("1234");
        assertEquals("1,234", AccountManager.format(bd));
    }
}
