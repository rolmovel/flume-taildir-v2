package org.keedio.watchdir;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;
import name.pachler.nio.file.ext.ExtendedWatchEventKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * 
 * This thread monitors the directory indicated in the constructor recursively. 
 * At the time believed a new file to all 
 * registered listeners are notified. 
 * <p>
 * Events deleted or modified files are not managed.
 *
 */
public class WatchDirObserver implements Runnable {

	private WatchService watcherSvc;
	private List<WatchDirListener> listeners;
	private static final Logger LOGGER= LoggerFactory
			.getLogger(WatchDirObserver.class);
	private final Map<WatchKey, Path> keys;
	private WatchDirFileSet set;
	
    public WatchDirObserver(WatchDirFileSet set) {
    	this.set = set;
    	keys = new HashMap<WatchKey, Path>();
    	listeners = new ArrayList<WatchDirListener>();
    	
		try {
			Path directotyToWatch = Paths.get(set.getPath());
	        watcherSvc = FileSystems.getDefault().newWatchService();
	        registerAll(java.nio.file.Paths.get(directotyToWatch.toString()));

		} catch (IOException e){
			LOGGER.info("No se puede monitorizar el directorio: " + set.getPath(), e);
		}
    }

    static <T> WatchEvent<T> castEvent(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    /**
     * Method used to record listeners. There must be at least one.
     * @param listener	Must implement WhatchDirListerner. See listeners implementations for more information
     */
    public void addWatchDirListener(WatchDirListener listener) {
    	listeners.add(listener);
    }
    
    protected void update(WatchDirEvent event) {
    	for (WatchDirListener listener:listeners) {
    		try{
        		listener.process(event);
    		} catch (WatchDirException e) {
    			LOGGER.info("Error procesando el listener", e);
    		}
    	}
    }

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     * @param  start	The initial path to monitor. 
     * 
     */
	
    private void registerAll(final java.nio.file.Path start) throws IOException {
        // register directory and sub-directories
        java.nio.file.Files.walkFileTree(start, new SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
                throws IOException {
                    register(Paths.get(dir.toString()));
                    return FileVisitResult.CONTINUE;
            }
        });
    }    
    
	private void register(Path dir) throws IOException {

		LOGGER.trace("WatchDir: register");
		
		// Solo nos preocupamos por los ficheros de nueva creacion
		
		WatchKey key = null;
		try {
			key = dir.register(watcherSvc, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_MODIFY, StandardWatchEventKind.ENTRY_DELETE, ExtendedWatchEventKind.ENTRY_RENAME_FROM, ExtendedWatchEventKind.ENTRY_RENAME_TO);
		} catch (UnsupportedOperationException e) {
			LOGGER.debug("Eventos no soportados. Registramos solo CREATE, DELETED, MODIFY");
			key = dir.register(watcherSvc, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_MODIFY, StandardWatchEventKind.ENTRY_DELETE);
		}
		Path prev = keys.get(key);

		LOGGER.info("Previous directory: " + prev);
		if (prev == null) {
			LOGGER.info("Registering directory: " + dir);
		} else {
			if (!dir.equals(prev)) {
				LOGGER.info("Updating previous directory: " + "-> " + prev + " to " + dir);
			}
		}

		keys.put(key, dir);

	}

    
    @Override
	public void run() {
    	
    	if (listeners.isEmpty()) {
    		LOGGER.error("No existen listeners. Finalizando");
    	} else {
    		try {
    			boolean fin = false;
    			
    			// En primer lugar procesamos todos los ficheros pre-existentes
    			if (set.isReadOnStartup()) {
        			for(String file:set.getExistingFiles()) {
        				WatchDirEvent event = new WatchDirEvent(file, "ENTRY_CREATE", set);
        				update(event);
        				LOGGER.debug("Fichero existente anteriormente:" + file + " .Se procesa");
        			}
    			}
    			
    			while (!fin) {
    				// wait for key to be signaled
    				WatchKey key;
    				key = watcherSvc.take();
    				Path dir = keys.get(key);

    				for (WatchEvent<?> event : key.pollEvents()) {
        				WatchEvent<Path> ev = cast(event);
    					Path name = ev.context();
    					Path path = dir.resolve(name);
    					
    					// Si se crea un nuevo directorio es necesario registrarlo de nuevo
    					if (java.nio.file.Files.isDirectory(java.nio.file.Paths.get(path.toString()), NOFOLLOW_LINKS))
    						registerAll(java.nio.file.Paths.get(path.toString()));
    					else {
    						if (set.haveToProccess(path.toString())) {
        						update(new WatchDirEvent(path.toString(), event.kind().name(), set));    							
    						}
    					}
    					
    				}

    				// reset key and remove from set if directory no longer
    				// accessible
    				key.reset();

    				Thread.sleep(1000);
    			}
    		} catch (InterruptedException e) {
    			LOGGER.info(e.getMessage(), e);
    		} catch (Exception e) {
    			LOGGER.info(e.getMessage(), e);
    		}
    	}
	}
    
    public static boolean match(String patterns, String string) {
    	
    	String[] splitPat = patterns.split(",");
    	boolean match = false;
    	
    	for (String pattern:splitPat) {
        	Pattern pat = Pattern.compile(pattern + "$");
        	Matcher mat = pat.matcher(string);
        	
        	match = match || mat.find();
        	
        	if (match) break;
    	}
    	
    	
    	return match;
    }

}
