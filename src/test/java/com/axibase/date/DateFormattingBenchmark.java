package com.axibase.date;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
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
public class DateFormattingBenchmark {
	private static final ZoneId UTC = ZoneId.of("UTC");
	private static final ZoneId MSK = ZoneId.of("Europe/Moscow");

	@Benchmark
	public String printCurrentIsoDateZoneOffset() {
		return DatetimeProcessorUtil.printIso8601(System.currentTimeMillis(), 'T', ZoneOffset.UTC, ZoneOffsetType.ISO8601, 3);
	}

	@Benchmark
	public String printCurrentIsoDateZoneIdUTC() {
		return DatetimeProcessorUtil.printIso8601(System.currentTimeMillis(), 'T', UTC, ZoneOffsetType.ISO8601, 3);
	}

	@Benchmark
	public String printCurrentIsoDateZoneIdMSK() {
		return DatetimeProcessorUtil.printIso8601(System.currentTimeMillis(), 'T', MSK, ZoneOffsetType.ISO8601, 3);
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include("DateFormattingBenchmark.*")
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
