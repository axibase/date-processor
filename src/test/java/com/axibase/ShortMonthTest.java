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
public class ShortMonthTest {
    private final ShortMonthAssertion assertion;
    private final Locale locale;
    private final String input;

    public ShortMonthTest(ShortMonthAssertion assertion, Locale locale, String input) {
        this.assertion = assertion;
        this.locale = locale;
        this.input = input;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(
                new Object[][]{
                        {ShortMonthAssertion.CAN_PARSE_SHORT_MONTH, Locale.ENGLISH, "Jan"},
                        {ShortMonthAssertion.CAN_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "Янв"},
                        {ShortMonthAssertion.CAN_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "ЯНВ"},
                        {ShortMonthAssertion.CAN_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "янв"},
                        {ShortMonthAssertion.CAN_PARSE_SHORT_MONTH, Locale.forLanguageTag("ru"), "янв."},
                        {ShortMonthAssertion.CANNOT_PARSE_FULL_MONTH, Locale.forLanguageTag("ru"), "Январь"},
                        {ShortMonthAssertion.CANNOT_PARSE_FULL_MONTH, Locale.forLanguageTag("ru"), "января"},
                        {ShortMonthAssertion.CANNOT_PARSE_FULL_MONTH, Locale.ENGLISH, "January"},
                        {ShortMonthAssertion.CANNOT_PARSE_DIGITS, Locale.ENGLISH, "1"},
                        {ShortMonthAssertion.CANNOT_PARSE_DIGITS, Locale.forLanguageTag("ru"), "1"},
                        {ShortMonthAssertion.CANNOT_PARSE_DIGITS, Locale.JAPANESE, "1"},
                }
        );
    }

    @Test
    public void testCanParse() {
        final DatetimeProcessor shortMonthFormatter = PatternResolver.createNewFormatter("MMM").withLocale(locale);
        assertThat(assertion.description, shortMonthFormatter.canParse(input), is(assertion.expectedResult));
    }

    private enum ShortMonthAssertion {
        CAN_PARSE_SHORT_MONTH("Should be able to parse short month with short month parser", true),
        CANNOT_PARSE_FULL_MONTH("Should be able to parse full month with short month parser", false),
        CANNOT_PARSE_DIGITS("Should not be able to parse numeric month with short month parser", false);

        private final String description;
        private final boolean expectedResult;

        ShortMonthAssertion(String description, boolean expectedResult) {
            this.description = description;
            this.expectedResult = expectedResult;
        }
    }
}
