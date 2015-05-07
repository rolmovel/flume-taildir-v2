package org.keedio.watchdir.listener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.keedio.watchdir.FileUtil;
import org.keedio.watchdir.WatchDirEvent;
import org.keedio.watchdir.metrics.MetricsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This worker proccess the xml file in order to extract the expeted events.
 * @author rolmo
 *
 */
public class FileEventHelper {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileEventHelper.class);

	FileEventSourceListener listener;
	WatchDirEvent event;
	
	public FileEventHelper(FileEventSourceListener listener, WatchDirEvent event) {
		this.listener = listener;
		this.event = event;
	}
	
	public void launchEvents() {
		try {
			Date inicio = new Date();
			int procesados = 0;
			
			readLines(event.getPath());
			
			long intervalo = new Date().getTime() - inicio.getTime();

			// Notificamos el tiempo de procesado para las metricas
			listener.getMetricsController().manage(new MetricsEvent(MetricsEvent.MEAN_FILE_PROCESS, intervalo));
			listener.getMetricsController().manage(new MetricsEvent(MetricsEvent.TOTAL_FILE_EVENTS, procesados));
			
		} catch (Exception e) {
			LOGGER.error("Error procesando el fichero: " + event.getPath());
			LOGGER.error("Se espera fichero XML: " + event.getSet().getTagName());
			
			LOGGER.error(e.getMessage());
		}
	}

	private void readLines(String path) throws Exception {

		BufferedReader lReader = new BufferedReader(new FileReader(new File(path)));
		
		//
		Long lastByte = 0L;
		if (listener.getFilesObserved().containsKey(path))
			lastByte = listener.getFilesObserved().get(path);
		else {
			// Probablemente se ha producido algún fallo de lo que no nos podamos recuperar
			// Ponemos el contador de eventos al final del del fichero
			LOGGER.debug("No se encontraba el registro en la tabla de contadores de lineas.");
			lastByte = getBytesSize(path);
			
			// seteamos el contador
			listener.getFilesObserved().put(path, new Long(getBytesSize(path)));
			
			return;
		}
		
		lReader.skip(lastByte);
		
		try {
			int lines = 0;
			String line;
			while ((line = lReader.readLine())!=null) {
				Event ev = EventBuilder.withBody(line.getBytes());
	    		// Calls to getChannelProccesor are synchronyzed
	    		listener.getChannelProcessor().processEvent(ev);
	            lines ++;
	            
	    		// Notificamos un evento de nuevo mensaje
	    		listener.getMetricsController().manage(new MetricsEvent(MetricsEvent.NEW_EVENT));
	    		
	    		// Actualizamos el número de eventos leidos
	    		listener.getFilesObserved().put(path, new Long(getBytesSize(path)));
			}
		} catch (IOException e) {
			LOGGER.error("Error al procesar el fichero: " + event.getPath(), e);
			throw e;
		} finally {
			lReader.close();
		}
	}

	public int countLines(String filename) throws IOException {
	    LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
	
	    int cnt = 0;

	    reader.skip(Long.MAX_VALUE);

	    cnt = reader.getLineNumber(); 
	    reader.close();
	    
	    return cnt;
	}
	
	public long getBytesSize(String filename) throws Exception
	{
        InputStream stream = null;
        try {
            URL url = new URL("file://" + filename);
            stream = url.openStream();
            return stream.available();
        } finally {
            stream.close();
        }
    }	
}
