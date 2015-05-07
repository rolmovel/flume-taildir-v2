package org.keedio.watchdir;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.keedio.watchdir.listener.FakeListener;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

public class WatchDirTest {
    {
        System.out.println("Executing test");

    }

    private Logger logger = Logger.getLogger("WatchDirTest");

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(new File(System.getProperty("java.io.tmpdir")));

    @Before
    public void setUp() {

    }

    @Test
    public void test() throws IOException, WatchDirException, InterruptedException {


        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "";
        String blacklist = "";
        
        File tstFolder = testFolder.newFolder("/tempFolder/");
        logger.info("tstFolder created");

        WatchDirFileSet set = new WatchDirFileSet(tstFolder.getAbsolutePath(), whitelist, blacklist, false, ".finished");
        WatchDirObserver monitor = new WatchDirObserver(set);
        logger.info("WatchDirObserver created");

        monitor.addWatchDirListener(listener);
        Thread t = new Thread(monitor);
        t.start();
        logger.info("Thread started");

        // Creamos el fichero
        testFolder.newFile("tempFolder/dd.dat");
        logger.info("dd.dat created");

        waitFor(20);
        verify(listener, atLeastOnce()).process(any(WatchDirEvent.class));

    }

    @Test
    public void test2() {

        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "";
        String blacklist = "";

        try {
            // Si no existen listeners sale del hilo
            File tstFolder = testFolder.newFolder("/tempFolder/");

            WatchDirFileSet set = new WatchDirFileSet(tstFolder.getAbsolutePath(), whitelist, blacklist,false, ".finished");
            WatchDirObserver monitor = new WatchDirObserver(set);
            Thread t = new Thread(monitor);
            t.start();

            waitFor(2);

            Assert.assertTrue(!t.isAlive());
            ;

        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {

        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "";
        String blacklist = "";

        try {
            // Si no existen listeners sale del hilo
            File tstFolder1 = testFolder.newFolder("/tempFolder1/");
            File tstFolder2 = testFolder.newFolder("/tempFolder2/");

            WatchDirFileSet set = new WatchDirFileSet(tstFolder1.getAbsolutePath(), whitelist, blacklist, false, ".finished");
            WatchDirObserver monitor = new WatchDirObserver(set);
            monitor.addWatchDirListener(listener);
            Thread t = new Thread(monitor);
            t.start();

            // Creamos el fichero en el directorio 1
            testFolder.newFile("tempFolder2/dd.dat");

            waitFor(20);
            verify(listener, times(0)).process(any(WatchDirEvent.class));

            // Creamos el fichero en el directorio 2
            testFolder.newFile("tempFolder1/dd.dat");

            waitFor(20);
            verify(listener, times(1)).process(any(WatchDirEvent.class));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreatedButBlacklisted() throws IOException, WatchDirException, InterruptedException {


        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "";
        String blacklist = "\\.xml,\\.filepart";
        
        File tstFolder = testFolder.newFolder("/tempFolder/");
        logger.info("tstFolder created");

        WatchDirFileSet set = new WatchDirFileSet(tstFolder.getAbsolutePath(), whitelist, blacklist, false, ".finished");
        WatchDirObserver monitor = new WatchDirObserver(set);
        logger.info("WatchDirObserver created");

        monitor.addWatchDirListener(listener);
        Thread t = new Thread(monitor);
        t.start();
        logger.info("Thread started");

        // Creamos el fichero
        testFolder.newFile("tempFolder/dd.filepart");
        logger.info("dd.dat created");

        waitFor(20);
        verify(listener, times(0)).process(any(WatchDirEvent.class));

    }
    
    @Test
    public void testCreatedButNotWhitelisted() throws IOException, WatchDirException, InterruptedException {


        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "\\.xml,\\.filepart";
        String blacklist = "";
        
        File tstFolder = testFolder.newFolder("/tempFolder/");
        logger.info("tstFolder created");

        WatchDirFileSet set = new WatchDirFileSet(tstFolder.getAbsolutePath(), whitelist, blacklist, false, ".finished");
        WatchDirObserver monitor = new WatchDirObserver(set);
        logger.info("WatchDirObserver created");

        monitor.addWatchDirListener(listener);
        Thread t = new Thread(monitor);
        t.start();
        logger.info("Thread started");

        // Creamos el fichero
        testFolder.newFile("tempFolder/dd.dat");
        logger.info("dd.dat created");

        waitFor(20);
        verify(listener, times(0)).process(any(WatchDirEvent.class));

    }
    
    @Test
    public void testCreatedAndWhitelisted() throws IOException, WatchDirException, InterruptedException {


        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "\\.xml,\\.filepart";
        String blacklist = "";
        
        File tstFolder = testFolder.newFolder("/tempFolder/");
        logger.info("tstFolder created");

        WatchDirFileSet set = new WatchDirFileSet(tstFolder.getAbsolutePath(), whitelist, blacklist,false, ".finished");
        WatchDirObserver monitor = new WatchDirObserver(set);
        logger.info("WatchDirObserver created");

        monitor.addWatchDirListener(listener);
        Thread t = new Thread(monitor);
        t.start();
        logger.info("Thread started");

        // Creamos el fichero
        testFolder.newFile("tempFolder/dd.filepart");
        logger.info("dd.dat created");

        waitFor(20);
        verify(listener, times(1)).process(any(WatchDirEvent.class));

    }

    @Test
    public void testCreatedAndBlacklisted() throws IOException, WatchDirException, InterruptedException {


        FakeListener listener = mock(FakeListener.class);
        //doReturn(22).when(listener).process(any(WatchEvent.class));

        String whitelist = "";
        String blacklist = "\\\\.xml,\\\\.filepart";
        
        File tstFolder = testFolder.newFolder("/tempFolder/");
        logger.info("tstFolder created");

        WatchDirFileSet set = new WatchDirFileSet(tstFolder.getAbsolutePath(), whitelist, blacklist, false, ".finished");
        WatchDirObserver monitor = new WatchDirObserver(set);
        logger.info("WatchDirObserver created");

        monitor.addWatchDirListener(listener);
        Thread t = new Thread(monitor);
        t.start();
        logger.info("Thread started");

        // Creamos el fichero
        testFolder.newFile("tempFolder/dd.dat");
        logger.info("dd.dat created");

        waitFor(20);
        verify(listener, times(1)).process(any(WatchDirEvent.class));

    }

    private static void waitFor(int seg) throws InterruptedException {
        for (int i = 1; i <= seg; i++) {
            System.out.println("... wating " + (seg - i) + " seconds");
            Thread.sleep(1000);
        }
    }

}
