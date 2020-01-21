package com.axibase.date;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

import static com.axibase.date.DatetimeProcessorUtil.MILLISECONDS_IN_SECOND;
import static com.axibase.date.DatetimeProcessorUtil.NANOS_IN_MILLIS;

@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ZonedDateTimeToUnixMillisBenchmark {
	@Param({"UTC_OFFSET", "UTC", "Europe/Moscow"})
	String zone;
	ZonedDateTime dateTime;

	@Setup
	public void setup() {
		ZoneId zoneId = "UTC_OFFSET".equals(zone) ? ZoneOffset.UTC : ZoneId.of(zone);
		dateTime = ZonedDateTime.now(zoneId);
	}

	@Benchmark
	public long usingDefault() {
		return dateTime.toInstant().toEpochMilli();
	}

	@Benchmark
	public long optimized() {
		return Math.addExact(dateTime.toEpochSecond() * MILLISECONDS_IN_SECOND, Math.floorDiv(dateTime.getNano(), NANOS_IN_MILLIS));
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include("ZonedDateTimeToUnixMillisBenchmark.*")
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
