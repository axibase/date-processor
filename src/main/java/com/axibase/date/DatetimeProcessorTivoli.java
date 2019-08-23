package com.axibase.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import static com.axibase.date.DatetimeProcessorUtil.timestampToZonedDateTime;
import static com.axibase.date.DatetimeProcessorUtil.toMillis;

class DatetimeProcessorTivoli implements DatetimeProcessor {
    private final ZoneId localZoneId = ZoneId.systemDefault();
    private final boolean parseZoneId;

    DatetimeProcessorTivoli(boolean parseZoneId) {
        this.parseZoneId = parseZoneId;
    }

    @Override
    public long parseMillis(String datetime) {
        return parseMillis(datetime, localZoneId);
    }

    @Override
    public long parseMillis(String datetime, ZoneId zoneId) {
        return toMillis(parse(datetime, zoneId));
    }

    @Override
    public ZonedDateTime parse(String datetime) {
        return parse(datetime, localZoneId);
    }

    @Override
    public ZonedDateTime parse(String datetime, ZoneId zoneId) {
        return parseZoneId ?
                DatetimeProcessorUtil.parseTivoliDateWithOffset(datetime).withZoneSameInstant(zoneId) :
                DatetimeProcessorUtil.parseTivoliDate(datetime).atZone(zoneId);
    }

    @Override
    public String print(long timestamp) {
        return print(timestamp, localZoneId);
    }

    @Override
    public String print(long timestamp, ZoneId zoneId) {
        return DatetimeProcessorUtil.printTivoliDate(timestampToZonedDateTime(timestamp, zoneId));
    }

    @Override
    public DatetimeProcessor withLocale(Locale locale) {
        return this;
    }
}
