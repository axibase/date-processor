package com.axibase.date;

public class SbReusePrinterBufferFactory implements OptimizedPrinterBufferFactory {
	private static final ThreadLocal<StringBuilder> builders = ThreadLocal.withInitial(StringBuilder::new);

	@Override
	public StringBuilder allocate(int size) {
		final StringBuilder stringBuilder = builders.get();
		stringBuilder.ensureCapacity(size);
		stringBuilder.setLength(0);
		return stringBuilder;
	}
}
