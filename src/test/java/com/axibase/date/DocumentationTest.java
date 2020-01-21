package com.axibase.date;

import java.time.ZoneId;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DocumentationTest {
    @Test
    public void testSqlDateTimeFormat() {
        final DatetimeProcessor formatter = PatternResolver.createNewFormatter("yyyy.MM.dd HH:mm:ss.SSS z");
        final long millis = formatter.parseMillis("2001.07.04 12:08:56.235 PDT");
        final ZoneId zoneId = ZoneId.of("America/Los_Angeles");
        assertThat(formatDateTime(millis, "yyyy.MM.dd G 'at' HH:mm:ss z", zoneId), is("2001.07.04 AD at 12:08:56 PDT"));
        assertThat(formatDateTime(millis, "eee, MMM d, ''yy", zoneId), is("Wed, Jul 4, '01"));
        assertThat(formatDateTime(millis, "hh:mm a", zoneId), is("12:08 PM"));
        assertThat(formatDateTime(millis, "hh 'o''clock' a, zzzz", zoneId), is("12 o'clock PM, Pacific Daylight Time"));
        assertThat(formatDateTime(millis, "K:mm a, z", zoneId), is("0:08 PM, PDT"));
        assertThat(formatDateTime(millis, "yyyyy.MMMM.dd GGG hh:mm a", zoneId), is("02001.July.04 AD 12:08 PM"));
        assertThat(formatDateTime(millis, "eee, d MMM yyyy HH:mm:ss Z", zoneId), is("Wed, 4 Jul 2001 12:08:56 -0700"));
        assertThat(formatDateTime(millis, "yyMMddHHmmssZ", zoneId), is("010704120856-0700"));
        assertThat(formatDateTime(millis, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", zoneId), is("2001-07-04T12:08:56.235-0700"));
        assertThat(formatDateTime(millis, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", zoneId), is("2001-07-04T12:08:56.235-07:00"));
        assertThat(formatDateTime(millis, "YYYY-'W'ww-u", zoneId), is("2001-W27-3"));
        assertThat(formatDateTime(millis, "u-eee", zoneId), is("3-Wed"));
        assertThat(formatDateTime(millis, "u", zoneId), is("3"));
        assertThat(formatDateTime(millis, "uuuu", zoneId), is("0003"));
        assertThat(formatDateTime(millis, "uu", zoneId), is("03"));
    }

    private static String formatDateTime(long millis, String pattern, ZoneId zoneId) {
        return PatternResolver.createNewFormatter(pattern).print(millis, zoneId);
    }
}
