package org.keedio.watchdir;

import org.junit.Assert;
import org.junit.Test;

public class WatchDirRegexTest {

	@Test
	public void testRegex() {
		
		Assert.assertTrue("Extension xml 1", WatchDirObserver.match("\\.xml", "prueba.xml"));
		Assert.assertFalse("Extension xml 2", WatchDirObserver.match("\\.xml", "pruebaxml"));
		Assert.assertFalse("Extension xml 3", WatchDirObserver.match("\\.xml", "pruebaxml.filepart"));
		Assert.assertFalse("Extension xml 4", WatchDirObserver.match("\\.xml", "prueba.xml.filepart"));
		Assert.assertFalse("Extension xml 5", WatchDirObserver.match("\\.xml,\\.filepart,\\.swx", "pruebaxmlfilepartswx"));
		Assert.assertFalse("Extension xml 6", WatchDirObserver.match("\\.xml,\\.filepart,\\.swx", "prueba.xmlfilepartswx"));
		Assert.assertTrue("Extension xml 7", WatchDirObserver.match("\\.xml,\\.filepart,\\.swx", "pruebaxmlfilepart.swx"));
		Assert.assertTrue("Extension xml 8", WatchDirObserver.match("\\.\\d+", "pruebaxmlfilepart.1"));
		
	}
	
}
