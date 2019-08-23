package com.axibase.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import static com.axibase.date.DatetimeProcessorUtil.timestampToZonedDateTime;

class DatetimeProcessorUnixMillis implements DatetimeProcessor {
    @Override
    public long parseMillis(String datetime) {
        return Long.parseLong(datetime);
    }

    @Override
    public long parseMillis(String datetime, ZoneId zoneId) {
        return Long.parseLong(datetime);
    }

    @Override
    public ZonedDateTime parse(String datetime) {
        return parse(datetime, ZoneId.systemDefault());
    }

    @Override
    public ZonedDateTime parse(String datetime, ZoneId zoneId) {
        return timestampToZonedDateTime(parseMillis(datetime), zoneId);
    }

    @Override
    public String print(long timestamp) {
        return "" + timestamp;
    }

    @Override
    public String print(long timestamp, ZoneId zoneId) {
        return print(timestamp);
    }

    @Override
    public DatetimeProcessor withLocale(Locale locale) {
        return this;
    }
}
