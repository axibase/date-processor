package com.axibase.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public interface DatetimeProcessor extends DatetimePatternTester {
    long parseMillis(String datetime);

    long parseMillis(String datetime, ZoneId zoneId);

    ZonedDateTime parse(String datetime);

    ZonedDateTime parse(String datetime, ZoneId zoneId);

    String print(long timestamp);

    String print(long timestamp, ZoneId zoneId);

    DatetimeProcessor withLocale(Locale locale);

    default boolean canParse(String date) {
        try {
            final ZonedDateTime parsed = parse(date);
            final int year = parsed.getYear();
            return year >= 1900 && year < 2200;
        } catch (Exception e) {
            return false;
        }
    }
}
