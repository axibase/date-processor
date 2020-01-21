package com.axibase.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class TimestampToZonedDateTimeBenchmark {
	private static final Instant MOCK = Instant.now();
	@Param({"UTC_OFFSET", "UTC", "Europe/Moscow"})
	String zone;
	ZoneId zoneId;

	@Setup
	public void setup() {
		zoneId = "UTC_OFFSET".equals(zone) ? ZoneOffset.UTC : ZoneId.of(zone);
	}

	@Benchmark
	public ZonedDateTime usingDefault() {
		return Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId);
	}

	@Benchmark
	public ZonedDateTime optimized() {
		final long millis = System.currentTimeMillis();
		final ZoneId z = zoneId;
		final ZonedDateTime result;
		if (z instanceof ZoneOffset) {
			long secs = Math.floorDiv(millis, 1000);
			int milliOfSecond = (int)Math.floorMod(millis, 1000);
			LocalDateTime ldt = LocalDateTime.ofEpochSecond(secs, milliOfSecond * 1_000_000, (ZoneOffset) z);
			result = ZonedDateTime.of(ldt, z);
		} else {
			final ZoneRules rules = z.getRules();
			if (rules.isFixedOffset()) {
				long secs = Math.floorDiv(millis, 1000);
				int milliOfSecond = (int)Math.floorMod(millis, 1000);
				ZoneOffset offset = rules.getOffset(MOCK);
				LocalDateTime ldt = LocalDateTime.ofEpochSecond(secs, milliOfSecond * 1_000_000, offset);
				result = ZonedDateTime.ofInstant(ldt, offset, z);
			} else {
				result = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), z);
			}
		}
		return result;
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include("TimestampToZonedDateTimeBenchmark.*")
			.jvmArgs("-server", "-Xmx1024M")
			.warmupIterations(1)
			.warmupTime(TimeValue.seconds(3))
			.measurementIterations(5)
			.addProfiler( GCProfiler.class )
			.forks(1)
			.build();
		new Runner(opt).run();
	}
}
