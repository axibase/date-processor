package com.axibase.jmh;

import java.util.concurrent.TimeUnit;

import com.axibase.date.DatetimeProcessorUtil;
import com.axibase.date.PrinterBufferFactoryManager;
import com.axibase.date.SbReusePrinterBufferFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
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
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class BufferFactoryBenchmark {
	@Param({"false", "true"})
	boolean optimized;

	@Setup(value = Level.Trial)
	public void setup() {
		PrinterBufferFactoryManager.setFactory(optimized ? new SbReusePrinterBufferFactory() : StringBuilder::new);
	}

	@Benchmark
	public long currentTime() {
		return System.currentTimeMillis();
	}

	@Benchmark
	public String printCurrentTimeAsIso() {
		return DatetimeProcessorUtil.printIso8601(System.currentTimeMillis(), true);
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include("BufferFactoryBenchmark.*")
			.jvmArgs("-server", "-Xmx64M")
			.warmupIterations(1)
			.warmupTime(TimeValue.seconds(3))
			.measurementIterations(20)
			.addProfiler( GCProfiler.class )
			.forks(2)
			.build();
		new Runner(opt).run();
	}
}
