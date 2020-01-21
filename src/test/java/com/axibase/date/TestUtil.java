package com.axibase.date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestUtil {
	public static void testPrint(String pattern, long timestamp, String expected) {
		assertThat(PatternResolver.createNewFormatter(pattern).print(timestamp), is(expected));
		final StringBuilder accumulator = new StringBuilder();
		PatternResolver.createNewFormatter(pattern).appendTo(timestamp, accumulator);
		assertThat(accumulator.toString(), is(expected));
	}
}
