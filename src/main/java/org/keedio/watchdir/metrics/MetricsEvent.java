package org.keedio.watchdir.metrics;

/**
 * 
 * Definition of event types used for management by the controller metric metric.
 *
 */
public class MetricsEvent {
	
	public static final int NEW_EVENT = 1;
	public static final int NEW_FILE = 2;
	public static final int MEAN_FILE_PROCESS = 3;
	public static final int TOTAL_FILE_EVENTS = 4;
	
	private int code;
	private long value = -1;
	
	public MetricsEvent(int code) {
		this.code = code;
	}
	
	public MetricsEvent(int code, long value) {
		this.code = code;
		this.value = value;
	}

	public int getCode() {
		return code;
	}
	
	public long getValue() {
		return this.value;
	}

}
