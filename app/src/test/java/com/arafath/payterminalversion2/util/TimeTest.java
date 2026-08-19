package com.arafath.payterminalversion2.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeTest {

    @Test
    public void relative_justNow() {
        assertEquals("just now", Time.relative(0));
    }

    @Test
    public void relative_minutesAgo() {
        assertEquals("5m ago", Time.relative(5 * 60 * 1000));
    }

    @Test
    public void relative_hoursAgo() {
        assertEquals("2h ago", Time.relative(2 * 60 * 60 * 1000));
    }

    @Test
    public void relative_daysAgo() {
        assertEquals("3d ago", Time.relative(3 * 24 * 60 * 60 * 1000));
    }

    @Test
    public void dateTime_format() {
        long millis = 1700000000000L;
        assertEquals("14 Nov 2023, 5:33 PM", Time.dateTime(millis));
    }
}