package com.arafath.payterminalversion2.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formats minor-unit amounts (paise) as BDT currency strings. */
public final class Money {
    private Money() {
    }

    public static String format(long paise) {
        return "৳" + NumberFormat.getNumberInstance(Locale.US).format(paise);
    }
}