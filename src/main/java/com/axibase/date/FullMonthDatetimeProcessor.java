package com.axibase.date;

import java.time.format.TextStyle;
import java.util.Locale;

class FullMonthDatetimeProcessor extends AbstractMonthDateTimeProcessor {
    FullMonthDatetimeProcessor(Locale locale) {
        super(locale, TextStyle.FULL, TextStyle.FULL_STANDALONE);
    }

    @Override
    public DatetimeProcessor withLocale(Locale locale) {
        return new FullMonthDatetimeProcessor(locale);
    }
}
