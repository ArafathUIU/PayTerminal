package com.arafath.payterminalversion2.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoneyTest {

    @Test
    public void formatZero_ReturnsBdt1() {
        assertEquals("৳0", Money.format(0));
    }

    @Test
    public void formatOneHundred_ReturnsBdt100() {
        assertEquals("৳100", Money.format(100));
    }

    @Test
    public void formatThousand_ReturnsBdt1000() {
        assertEquals("৳1,000", Money.format(1000));
    }

    @Test
    public void formatNegative_ReturnsMinusBdt() {
        assertEquals("৳-50", Money.format(-50));
    }
}