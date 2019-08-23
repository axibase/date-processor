package com.axibase.date;

import java.time.format.TextStyle;
import java.util.Locale;

class ShortMonthDateTimeProcessor extends AbstractMonthDateTimeProcessor {
    ShortMonthDateTimeProcessor(Locale locale) {
        super(locale, TextStyle.SHORT, TextStyle.SHORT_STANDALONE);
    }

    @Override
    public DatetimeProcessor withLocale(Locale locale) {
        return new ShortMonthDateTimeProcessor(locale);
    }
}
