package org.keedio.watchdir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	private static final Logger LOGGER= LoggerFactory
			.getLogger(FileUtil.class);
	
	public static String getFileKey(Path path){
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
			return path.toString();
		else
			try{
				if (Files.exists(path))
					return Files.readAttributes(path, BasicFileAttributes.class).
							fileKey().toString();
				else
					return null;
			}catch (IOException e){
				LOGGER.warn(e.getMessage(),e);
				return null;
			}
	}
}
