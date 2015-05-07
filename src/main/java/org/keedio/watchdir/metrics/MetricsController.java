package org.keedio.watchdir.metrics;

import org.apache.flume.instrumentation.MonitoredCounterGroup;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
*This class represents the controller metrics to publish to the source. 
*Extends MonitoredCounterGroup class to allow the publication of JMX metrics 
*following the mechanism established by Flume. 
*/
public class MetricsController extends MonitoredCounterGroup implements MetricsMBean {

	private Meter meterEvents;
	private Meter meterFiles;
	private Histogram meanProcessTime;
	private Histogram totalFileEvents;
	private MetricRegistry metrics;	
	
	private static final String[] ATTRIBUTES = { "source.meter.events",
		"source.meter.files", "source.mean.process.time", "source.total.file.events" };
	
	public MetricsController() {
		super(MonitoredCounterGroup.Type.SOURCE, MetricsController.class.getName(), ATTRIBUTES);
		
		metrics = new MetricRegistry();
		meterEvents = metrics.meter("events");
		meterFiles = metrics.meter("files");
		meanProcessTime = metrics.histogram("meanProcessTime");
		totalFileEvents = metrics.histogram("totalFileEvents");
		
	}

	/**
	 * This method manages metric based on events received.
	 * <p>
	 * For new metrics will need to create the corresponding event type in 
	 * MetricsEvent class and then define their behavior here
	 * @param  event	event to manage
	 * @return      
	 * @see        
	 */
	public void manage(MetricsEvent event) {
		switch (event.getCode()) {
		case MetricsEvent.NEW_FILE:
			meterFiles.mark();
			break;
		case MetricsEvent.NEW_EVENT:
			meterEvents.mark();
			break;
		case MetricsEvent.TOTAL_FILE_EVENTS:
			totalFileEvents.update(event.getValue());
			break;
		case MetricsEvent.MEAN_FILE_PROCESS:
			meanProcessTime.update(event.getValue());
			break;
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public long getTotalEvents() {
		return meterEvents.getCount();
	}

	@Override
	public long getTotalFiles() {
		return meterFiles.getCount();
	}

	@Override
	public double getMeanProcessTime() {
		return meanProcessTime.getSnapshot().getMean();
	}

	@Override
	public double getMeanFileEvents() {
		return totalFileEvents.getSnapshot().getMean();
	}

	
}
