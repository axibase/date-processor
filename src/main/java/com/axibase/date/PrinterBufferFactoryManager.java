package com.axibase.date;

public class PrinterBufferFactoryManager {
	private static OptimizedPrinterBufferFactory instance = StringBuilder::new;

	public static void setFactory(OptimizedPrinterBufferFactory bufferFactory) {
		if (bufferFactory == null) {
			throw new NullPointerException("bufferFactory must not be null");
		}
		instance = bufferFactory;
	}

	public static OptimizedPrinterBufferFactory getInstance() {
		return instance;
	}
}
