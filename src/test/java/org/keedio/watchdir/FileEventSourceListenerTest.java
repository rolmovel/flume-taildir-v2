package org.keedio.watchdir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.flume.Channel;
import org.apache.flume.ChannelSelector;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.ReplicatingChannelSelector;
import org.apache.flume.conf.Configurables;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.WatchDirObserver;
import org.keedio.watchdir.listener.FakeListener;
import org.keedio.watchdir.listener.FileEventSourceListener;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import static org.hamcrest.core.StringContains.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FileEventSourceListenerTest {

	
	FileEventSourceListener listener;
	File tstFolder1;
	File tstFolder2;
	File tstFolder3;

	@Mock
	FakeListener mock = mock(FakeListener.class);
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder(new File(System.getProperty("java.io.tmpdir")));

	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
	@Before
	public void setUp() throws IOException{
        tstFolder1 = testFolder.newFolder("/tmp1/");
        tstFolder2 = testFolder.newFolder("/tmp2/");
        tstFolder3 = testFolder.newFolder("/tmp3/");
		
		listener = new FileEventSourceListener();
		
		Channel channel = new MemoryChannel();
		Context context = new Context();
		context.put("dirs.1.dir", tstFolder1.getAbsolutePath());
		context.put("dirs.2.dir", tstFolder2.getAbsolutePath());
		context.put("event.terminator", "|#]");
		context.put("keep-alive", "1");
		context.put("capacity", "100000");
		context.put("transactionCapacity", "100000");
		context.put("blacklist", "");
		context.put("whitelist", "");
		context.put("pathtoser", testFolder.getRoot() + "/test.ser");
		context.put("timetoser", "5");

		Configurables.configure(listener, context);
		Configurables.configure(channel, context);
		
		ChannelSelector rcs = new ReplicatingChannelSelector();
		rcs.setChannels(Lists.newArrayList(channel));

		listener.setChannelProcessor(new ChannelProcessor(rcs));
		
		listener.configure(context);
		
		listener.start();;
		
	}
	
	@After
	public void finish() {
		listener.stop();
	}
	
	@Test
	public void testOnGoing() {
		System.out.println();
		Assert.assertTrue("El hilo esta corriendo", "START".equals(listener.getLifecycleState().toString()));
	}
	
	@Test
	public void testFileModified() {
		
		try {
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			

            // Creamos el fichero en el directorio 1
        	FileUtils.copyFile(new File("src/test/resources/test2.txt"), testFolder.newFile("tmp1/test2.txt"));
        	
        	Collection<String> col = new ArrayList<String>();
        	col.add("Evento 20");
        	col.add("Evento 21");
        	col.add("Evento 22");
        	col.add("Evento 23");
        	FileUtils.writeLines(FileUtils.getFile(testFolder.getRoot() + "/tmp1/test2.txt"), col, true);
        	col.clear();
        	col.add("Evento 24");
        	col.add("Evento 25");
        	col.add("Evento 26");
        	col.add("Evento 27");
        	col.add("Evento 28");
        	col.add("Evento 29");
        	FileUtils.writeLines(FileUtils.getFile(testFolder.getRoot() + "/tmp1/test2.txt"), col, true);
            Thread.sleep(20000);
            verify(mock, atLeast(2)).process(any(WatchDirEvent.class));


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }
		
	}
	
	@Test
	@Ignore
	public void testFileModifiedNotObserved() {
		
		try {
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			

            // Creamos el fichero en el directorio 1
        	FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp3/test2.txt"));

            Thread.sleep(20000);
            verify(mock, times(0)).process(any(WatchDirEvent.class));
            Assert.assertFalse("No se ha creado el fichero", new File("tmp3/test.xml.finished").exists());


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }
		
	}

	@Test
	@Ignore
	public void testFileModifiedTwoDirectories() throws Exception {
		
			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			

            // Creamos el fichero en el directorio 1
        	FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp1/test2.txt"));
        	FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp2/test2.txt"));
        	FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp3/test2.txt"));

            Thread.sleep(20000);
            verify(mock, times(2)).process(any(WatchDirEvent.class));
            
            // Los ficheros .finished han tenido que ser generados.
            thrown.expectMessage(containsString("already exists in the test folder"));
            testFolder.newFile("tmp1/test.xml.finished").exists();
            testFolder.newFile("tmp2/test.xml.finished").exists();


	}
	
	@Test
	@Ignore
	public void testExistingFiles() throws Exception {
		
			// Creamos el fichero en el directorio 1
    		FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp1/test.txt"));
    		FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp1/test2.txt"));
    		FileUtils.copyFile(new File("test/resources/test2.txt"), testFolder.newFile("tmp3/test.txt"));

			// Registramos el FakeListener en todos los monitores
			for (WatchDirObserver observer: listener.getMonitor()) {
				observer.addWatchDirListener(mock);
			}
			
            Thread.sleep(20000);
            verify(mock, times(2)).process(any(WatchDirEvent.class));
            
            // Los ficheros .finished han tenido que ser generados.
            thrown.expectMessage(containsString("already exists in the test folder"));
            testFolder.newFile("tmp1/test.xml.finished").exists();
            testFolder.newFile("tmp1/test2.xml.finished").exists();


	}


}
