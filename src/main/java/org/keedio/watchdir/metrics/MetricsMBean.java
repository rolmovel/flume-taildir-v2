package org.keedio.watchdir.metrics;

import javax.management.MXBean;

@MXBean
public interface MetricsMBean {

	
	public long getTotalEvents();
	public long getTotalFiles();
	public double getMeanProcessTime();
	public double getMeanFileEvents();
	
}
