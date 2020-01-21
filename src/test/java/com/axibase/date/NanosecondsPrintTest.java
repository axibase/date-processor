package com.axibase.date;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NanosecondsPrintTest {
	private final ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(2019, 12, 5, 14, 17, 21, 123456789), ZoneOffset.of("+02:00"));

	@Test
	public void testPrintIso() {
		final String formatted = PatternResolver.createNewFormatter(NamedPatterns.ISO_NANOS).print(dateTime);
		assertThat(formatted, is("2019-12-05T14:17:21.123456789+02:00"));
	}

	@Test
	public void testPrintLocal() {
		final String formatted = PatternResolver.createNewFormatter("yyyy-MM-dd HH:mm:ss.SSSSSSSSS").print(dateTime);
		assertThat(formatted, is("2019-12-05 14:17:21.123456789"));
	}

	@Test
	public void testPrintCustom() {
		final String formatted = PatternResolver.createNewFormatter("MMM, dd yyyy HH:mm:ss.SSSSSSSSS").withLocale(Locale.US).print(dateTime);
		assertThat(formatted, is("Dec, 05 2019 14:17:21.123456789"));
	}
}
