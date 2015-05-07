package org.keedio.watchdir.listener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SerializeFilesThread implements Runnable {

	private FileEventSourceListener listener;
	private String path;
	private int seconds;
	
	public SerializeFilesThread(FileEventSourceListener listener, String path, int seconds) {
		this.listener = listener;
		this.path = path;
		this.seconds = seconds;
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				fromMapToSerFile();

				Thread.sleep(seconds * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Map<String, Long> getMapFromSerFile() throws Exception {
		Map<String, Long> map = null;
		
		FileInputStream fis = new FileInputStream(path);
		ObjectInputStream ois = new ObjectInputStream(fis);
		map = (Map<String, Long>) ois.readObject();
			
		return map;
			
	}

	public void fromMapToSerFile() throws Exception {
		FileOutputStream fos = new FileOutputStream(path);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(listener.getFilesObserved());			
	}

}
