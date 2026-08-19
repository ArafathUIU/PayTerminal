package com.arafath.payterminalversion2.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Time {
    private Time() {
    }

    /** e.g. "2m ago" or "3h ago" for recent lists. */
    public static String relative(long millis) {
        long diff = Math.max(0, System.currentTimeMillis() - millis);
        long minutes = diff / 60000;
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = hours / 24;
        return days + "d ago";
    }

    public static String dateTime(long millis) {
        return new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.US).format(new Date(millis));
    }
}