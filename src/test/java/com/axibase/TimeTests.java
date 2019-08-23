package com.axibase;

import com.axibase.date.DatetimeProcessorUtil;
import com.axibase.date.PatternResolver;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

public class TimeTests {
    @Test
    public void testParse() {
        long tz = PatternResolver.createNewFormatter("dd.MM.yyyy HH:mm:ss.SSS")
                .parseMillis("29.09.2015 00:00:00.000", ZoneId.of("America/Port-au-Prince"));
        long utc = PatternResolver.createNewFormatter("dd.MM.yyyy HH:mm:ss.SSS")
                .parseMillis("29.09.2015 00:00:00.000", ZoneOffset.UTC);
        long diff = tz - utc;
        long diffHours = diff / (60 * 60 * 1000);
        assertEquals(4, diffHours);
    }

    @Test
    public void testDefaultFields() throws ParseException {
        final long now = System.currentTimeMillis();
        final String[] formats = {
                "yyyy-MM-dd HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ssZZ",
                "yyyy-MM-dd HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd",
                "yyyy-MM",
                "MM",
                "1yyMMddHHmmssSSS"
        };
        for (String format : formats) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            final String date = simpleDateFormat.format(now);
            long resolvedBySdf = simpleDateFormat.parse(date).getTime();
            long resolvedJsr310 =  PatternResolver.createNewFormatter(format).parseMillis(date);
            assertThat("Parsed millis mismatch for timestamp=" + now + " and format=" + format, resolvedBySdf, is(resolvedJsr310));
        }
    }

    @Test
    public void testParseTivoli() {
        final String date = "2018-12-01T15:10:00.000Z";
        final String tivoliDate = "1181201151000000";
        final long parsed = PatternResolver.createNewFormatter("1yyMMddHHmmssSSS")
                .parseMillis(tivoliDate, ZoneId.of("UTC"));
        assertThat(parsed, is(parseISO8601(date)));
    }

    @Test
    public void testParseIso8601WithoutTimeZone() {
        final String date = "2018-12-01T15:10:00.000Z";
        final long parsed = PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSS")
                .parseMillis(date.substring(0, date.length() - 1), ZoneId.of("UTC"));
        assertThat(parsed, is(parseISO8601(date)));
    }

    @Test
    public void testParseOptimized() {
        final long time = Instant.parse("2017-09-12T07:47:59.999Z").toEpochMilli();
        assertThat(parseISO8601("2017-09-12T07:47:59.999Z"), is(time));
        assertThat(parseISO8601("2017-09-12T07:47:59Z"), is(time / 1000 * 1000));
        assertThat(parseISO8601("2017-09-12T07:47:59.9999Z"), is(time));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOptimizedNoZoneException() {
        parseISO8601("2017-09-12T07:47:59.999999");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOptimizedTooMuchFractionOfSecondException() {
        parseISO8601("2017-09-12T07:47:59.9999999999Z");
    }

    @Test
    public void testPreprocessing() {
        String date = "2018-08-12T09:56:10.998Z";
        final long timestamp = parseISO8601(date);
        assertThat(parseMillis("2018-08-12T09:56:10.998Z", "yyyy-MM-ddTHH:mm:ss.SSSZ"), is(timestamp));
        assertThat(parseMillis("2018-08-12T09:56:10.998 Z", "yyyy-MM-ddTHH:mm:ss.SSS Z"), is(timestamp));
        assertThat(parseMillis("2018-08-12T12:56:10.998+03:00", "yyyy-MM-ddTHH:mm:ss.SSSXXX"), is(timestamp));
        assertThat(parseMillis("2018-08-12T12:56:10.998 +03:00", "yyyy-MM-ddTHH:mm:ss.SSS XXX"), is(timestamp));
        assertThat(parseMillis("2018-08-12T12:56:10.998 +0300", "yyyy-MM-ddTHH:mm:ss.SSS Z"), is(timestamp));
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSSZ").print(timestamp, ZoneOffset.UTC), is(date));
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSSZ").print(timestamp, ZoneOffset.ofHours(1)), is("2018-08-12T10:56:10.998+0100"));
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSSXXX").print(timestamp, ZoneOffset.ofHours(1)), is("2018-08-12T10:56:10.998+01:00"));
    }

    @Test
    public void testNamedFormats() {
        final String datetime = "2018-01-01T14:00:01.000Z";
        final long timestamp = parseISO8601(datetime);
        assertThat(parseMillis("" + timestamp, "milliseconds"), is(timestamp));
        assertThat(parseMillis(Long.toString(timestamp / 1000), "seconds"), is(timestamp));
        assertThat(PatternResolver.createNewFormatter("tivoli").parseMillis("1180101140001000", ZoneId.of("UTC")), is(timestamp));
        assertThat(PatternResolver.createNewFormatter("iso").parseMillis(datetime, ZoneId.of("UTC")), is(timestamp));
    }

    @Test
    public void testCanParse() {
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ssZ").canParse("2018-01-01T14:00:01.000Z"), is(false));
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSSZ").canParse("2018-01-01T14:00:01.000Z"), is(true));
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSSSZ").canParse("2018-01-01T14:00:01.000Z"), is(false));
        assertThat(PatternResolver.createNewFormatter("yyyy-MM-ddTHH:mm:ss.SSSSZ").canParse("2018-01-01T14:00:01.0000Z"), is(true));
        assertThat(PatternResolver.createNewFormatter("milliseconds").canParse("1516020195345"), is(true));
        assertThat(PatternResolver.createNewFormatter("seconds").canParse("1516020195"), is(true));
        assertThat(PatternResolver.createNewFormatter("seconds").canParse("1516020195.345"), is(true));
    }

    @Test
    public void testPrintUnixSeconds() {
        assertThat(PatternResolver.createNewFormatter("seconds").print(1516020195000L), is("1516020195"));
        assertThat(PatternResolver.createNewFormatter("seconds").print(1516020195345L), is("1516020195.345"));
        assertThat(PatternResolver.createNewFormatter("seconds").print(1516020195045L), is("1516020195.045"));
        assertThat(PatternResolver.createNewFormatter("seconds").print(1516020195005L), is("1516020195.005"));
    }

    private long parseMillis(String date, String pattern) {
        return PatternResolver.createNewFormatter(pattern).parseMillis(date);
    }
    
    private long parseISO8601(String date) {
        return DatetimeProcessorUtil.parseIso8601AsOffsetDateTime(date, 'T').toInstant().toEpochMilli();
    }
}
