package org.keedio.watchdir;

public interface WatchDirListener {

	
	/**
	 * In this method the logic is implemented to perform once notified the file creation event.
	 * @param event	Event to process
	 * @throws WatchDirException
	 */
	public void process(WatchDirEvent event) throws WatchDirException;

}
