package org.keedio.watchdir;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.keedio.watchdir.listener.SerializeFilesThread;

public class SerializeFilesThreadTest {

	
	@Test
	public void testSerializacion() throws Exception {
		Map<String, Long> map = new HashMap<>();
		map.put("1", 0L);
		map.put("2", 0L);
		map.put("3", 0L);
		map.put("4", 0L);
		
		SerializeFilesThread ser = new SerializeFilesThread(null, "/tmp/test.ser", 5);
		ser.fromMapToSerFile();
		Map<String, Long> aux = ser.getMapFromSerFile();
		
		Assert.assertEquals(map, aux);
		
	}
	
}
