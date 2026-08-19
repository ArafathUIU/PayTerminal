package com.arafath.payterminalversion2.util;

import org.junit.*;
import static org.junit.Assert.*;

public class TimeTest {

    @BeforeClass
    public static void setUpClass() {
        // Override the currentTimeMillis() used by Time.relative()
        // to use a fixed reference time of 500ms after epoch.
        Time.setRefTime(500L);
    }

    @Test
    public void relative_justNow() {
        assertEquals("just now", Time.relative(0));
    }

    @Test
    public void relative_minutesAgo() {
        // With ref time = 500ms: diff = 500 - (-299500) = 300000ms = 5min
        assertEquals("5m ago", Time.relative(-299500));
    }

    @Test
    public void relative_hoursAgo() {
        // With ref time = 500ms: diff = 500 - (-7199500) = 7200000ms = 2h
        assertEquals("2h ago", Time.relative(-7199500));
    }

    @Test
    public void relative_daysAgo() {
        // With ref time = 500ms: diff = 500 - (-259199500) = 259200000ms = 3d
        assertEquals("3d ago", Time.relative(-259199500));
    }

    @Test
    public void dateTime_format() {
        long millis = 1700000000000L;
        assertEquals("14 Nov 2023, 5:33 PM", Time.dateTime(millis));
    }
}