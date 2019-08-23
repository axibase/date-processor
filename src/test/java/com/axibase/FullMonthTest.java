package com.axibase;

import com.axibase.date.DatetimeProcessor;
import com.axibase.date.PatternResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class FullMonthTest {
    private final FullMonthAssertion assertion;
    private final Locale locale;
    private final String input;

    public FullMonthTest(FullMonthAssertion assertion, Locale locale, String input) {
        this.assertion = assertion;
        this.locale = locale;
        this.input = input;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(
                new Object[][]{
                        {FullMonthAssertion.CAN_PARSE_FULL_MONTH, Locale.forLanguageTag("ru"), "Январь"},
                        {FullMonthAssertion.CAN_PARSE_FULL_MONTH, Locale.forLanguageTag("ru"), "январь"},
                        {FullMonthAssertion.CAN_PARSE_FULL_MONTH, Locale.forLanguageTag("ru"), "ЯНВАРЬ"},
                        {FullMonthAssertion.CAN_PARSE_FULL_MONTH, Locale.forLanguageTag("ru"), "января"},
                        {FullMonthAssertion.CAN_PARSE_FULL_MONTH, Locale.ENGLISH, "January"},
                        {FullMonthAssertion.CANNOT_PARSE_SHORT_MONTH, Locale.ENGLISH, "Jan"},
                        {FullMonthAssertion.CANNOT_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "Янв"},
                        {FullMonthAssertion.CANNOT_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "ЯНВ"},
                        {FullMonthAssertion.CANNOT_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "янв"},
                        {FullMonthAssertion.CANNOT_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "янв."},
                        {FullMonthAssertion.CANNOT_PARSE_DIGITS, Locale.ENGLISH, "1"},
                        {FullMonthAssertion.CANNOT_PARSE_DIGITS, Locale.forLanguageTag("ru"), "1"},
                        {FullMonthAssertion.CANNOT_PARSE_DIGITS, Locale.JAPANESE, "1"},
                }
        );
    }

    @Test
    public void testCanParse() {
        final DatetimeProcessor shortMonthFormatter = PatternResolver.createNewFormatter("MMMM").withLocale(locale);
        assertThat(assertion.description, shortMonthFormatter.canParse(input), is(assertion.expectedResult));
    }

    private enum FullMonthAssertion {
        CAN_PARSE_FULL_MONTH("Should be able to parse full month with full month parser", true),
        CANNOT_PARSE_SHORT_MONTH("Should be able to parse short month with full month parser", false),
        CANNOT_PARSE_DIGITS("Should not be able to parse numeric month with full month parser", false);

        private final String description;
        private final boolean expectedResult;

        FullMonthAssertion(String description, boolean expectedResult) {
            this.description = description;
            this.expectedResult = expectedResult;
        }
    }
}
