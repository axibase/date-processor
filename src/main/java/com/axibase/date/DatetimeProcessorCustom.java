package com.axibase.date;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Locale;

import static com.axibase.date.DatetimeProcessorUtil.timestampToZonedDateTime;
import static com.axibase.date.DatetimeProcessorUtil.toMillis;

class DatetimeProcessorCustom implements DatetimeProcessor {
    private final DateTimeFormatter dateTimeFormatter;
    private final ZoneId zoneId;

    DatetimeProcessorCustom(DateTimeFormatter dateTimeFormatter, ZoneId zoneId) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.zoneId = zoneId;
    }

    @Override
    public long parseMillis(String datetime) {
        return toMillis(parse(datetime, zoneId));
    }

    @Override
    public long parseMillis(String datetime, ZoneId zoneId) {
        return toMillis(parse(datetime, zoneId));
    }

    @Override
    public ZonedDateTime parse(String datetime) {
        return parseMillisFailSafe(datetime, zoneId);
    }

    @Override
    public ZonedDateTime parse(String datetime, ZoneId zoneId) {
        return parseMillisFailSafe(datetime, zoneId);
    }

    @Override
    public String print(long timestamp) {
        return print(timestamp, zoneId);
    }

    @Override
    public String print(long timestamp, ZoneId zoneId) {
        return timestampToZonedDateTime(timestamp, zoneId).format(dateTimeFormatter);
    }

    @Override
    public String print(ZonedDateTime zonedDateTime) {
        return dateTimeFormatter.format(zonedDateTime);
    }

    @Override
    public void appendTo(long timestamp, StringBuilder accumulator) {
        dateTimeFormatter.formatTo(timestampToZonedDateTime(timestamp, zoneId), accumulator);
    }

    @Override
    public DatetimeProcessor withLocale(Locale locale) {
        if (locale.equals(dateTimeFormatter.getLocale())) {
            return this;
        }
        return new DatetimeProcessorCustom(dateTimeFormatter.withLocale(locale), zoneId);
    }

    @Override
    public DatetimeProcessor withDefaultZone(ZoneId zoneId) {
        return this.zoneId.equals(zoneId) ? this : new DatetimeProcessorCustom(dateTimeFormatter, zoneId);
    }

    private static LocalDate resolveDateFromTemporal(TemporalAccessor parsed) {
        final LocalDate query = parsed.query(TemporalQueries.localDate());
        if (query != null) {
            return query;
        }
        int year = DatetimeProcessorUtil.UNIX_EPOCH_YEAR;
        int month = 1;
        int day = 1;
        if (parsed.isSupported(ChronoField.YEAR)) {
            year = parsed.get(ChronoField.YEAR);
        } else if (parsed.isSupported(ChronoField.YEAR_OF_ERA)) {
            year = parsed.get(ChronoField.YEAR_OF_ERA);
        } else if (parsed.isSupported(IsoFields.WEEK_BASED_YEAR)) {
            year = parsed.get(IsoFields.WEEK_BASED_YEAR);
        }
        if (parsed.isSupported(ChronoField.MONTH_OF_YEAR)) {
            month = parsed.get(ChronoField.MONTH_OF_YEAR);
        } else if (parsed.isSupported(IsoFields.QUARTER_OF_YEAR)) {
            int quarter = parsed.get(IsoFields.QUARTER_OF_YEAR);
            month = quarter * 3 - 2;
        }
        if (parsed.isSupported(ChronoField.DAY_OF_MONTH)) {
            day = parsed.get(ChronoField.DAY_OF_MONTH);
        }
        return LocalDate.of(year, month, day);
    }

    private static LocalTime resolveTimeFromTemporal(TemporalAccessor parsed) {
        final LocalTime query = parsed.query(TemporalQueries.localTime());
        if (query != null) {
            return query;
        }
        int hour = 0;
        int minute = 0;
        int second = 0;
        int nanos = 0;
        if (parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
            hour = parsed.get(ChronoField.HOUR_OF_DAY);
        }
        if (parsed.isSupported(ChronoField.MINUTE_OF_HOUR)) {
            minute = parsed.get(ChronoField.MINUTE_OF_HOUR);
        }
        if (parsed.isSupported(ChronoField.SECOND_OF_MINUTE)) {
            second = parsed.get(ChronoField.SECOND_OF_MINUTE);
        }
        if (parsed.isSupported(ChronoField.NANO_OF_SECOND)) {
            nanos = parsed.get(ChronoField.NANO_OF_SECOND);
        }
        return LocalTime.of(hour, minute, second, nanos);
    }

    /**
     * Parse datetime string  {@param value} using {@param formatter}. Provides default values for most fields,
     * so should be equivalent to SimpleDateFormat and joda-time for string-to-long timestamp converting operations.
     * @param datetime datetime as string
     * @param defaultZoneId zone id used if can not be resolved by formatter.
     * @return millis from epoch
     */
    private ZonedDateTime parseMillisFailSafe(String datetime, ZoneId defaultZoneId) {
        final TemporalAccessor parsed = dateTimeFormatter.parse(datetime);
        final LocalDate localDate = resolveDateFromTemporal(parsed);
        final LocalTime localTime = resolveTimeFromTemporal(parsed);
        ZoneId zone = parsed.query(TemporalQueries.zone());
        if (zone == null) {
            zone = defaultZoneId;
        }
        return ZonedDateTime.of(localDate, localTime, zone);
    }

}
