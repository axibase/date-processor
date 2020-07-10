package com.axibase.date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class OnMissingDateComponentTest {
    private final String date;
    private final String format;
    private final OnMissingDateComponentAction onMissingDateComponentAction;
    private final ZonedDateTime expected;

    public OnMissingDateComponentTest(String date, String format, OnMissingDateComponentAction onMissingDateComponentAction, ZonedDateTime expected) {
        this.date = date;
        this.format = format;
        this.onMissingDateComponentAction = onMissingDateComponentAction;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        ZonedDateTime expected = DatetimeProcessorUtil.parseIso8601AsZonedDateTime("1970-07-26T16:40:22Z");
        return Arrays.asList(
                new Object[][]{
                        {"Jul 26 16:40:22", "MMM dd HH:mm:ss", OnMissingDateComponentAction.SET_ZERO, expected},
                        {"Jul 26 16:40:22", "MMM dd HH:mm:ss", OnMissingDateComponentAction.SET_CURRENT, expected.withYear(now.getYear())},
                        {"Jul 16:40:22", "MMM HH:mm:ss", OnMissingDateComponentAction.SET_CURRENT, expected.withYear(now.getYear()).withDayOfMonth(now.getDayOfMonth())},
                        {"16:40:22", "HH:mm:ss", OnMissingDateComponentAction.SET_CURRENT, expected.withYear(now.getYear()).withDayOfMonth(now.getDayOfMonth()).withMonth(now.getMonthValue())},
                }
        );
    }

    @Test
    public void testParseWithMissingDateComponent() {
        final DatetimeProcessor processor = PatternResolver.createNewFormatter(format, ZoneId.of("UTC"), onMissingDateComponentAction)
                .withLocale(Locale.US);
        final ZonedDateTime result = processor.parse(date);
        assertThat(result.toInstant(), is(expected.toInstant()));
    }
}
