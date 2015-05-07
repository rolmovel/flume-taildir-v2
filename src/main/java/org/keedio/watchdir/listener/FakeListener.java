package org.keedio.watchdir.listener;

import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirException;
import org.keedio.watchdir.WatchDirListener;

/**
 * 
 * Very simple fake example. Implements WatchDirListener
 *
 */
public class FakeListener implements WatchDirListener {

	@Override
	public void process(WatchDirEvent event) throws WatchDirException {
		System.out.println("Got event: " + event.getPath() + " " + event.getType());
	}


}
