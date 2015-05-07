package org.keedio.watchdir;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thos class include characteristics of the file to be monitorized:
 * Path, path of the directory 
 * Tag name, name of tag expected to be in the xml files
 * Tag level, level of tag expected to be in the xml files
 * Whitelist, files to monitorize
 * Blacklist, excluded files
 * 
 * @author rolmo
 *
 */
public class WatchDirFileSet {
	
	private static final Logger LOGGER= LoggerFactory
			.getLogger(WatchDirFileSet.class);
	private String path;
	private String tagName;
	private int tagLevel;
	private String whitelist;
	private String blacklist;
	private String suffix = ".finished";
	private Set<String> existingFiles;
	private boolean readOnStartup;

	public WatchDirFileSet(String path,
			String whitelist, String blacklist, boolean readOnStartup, String suffix) {
		super();
		this.path = path==null?"":path;
		this.whitelist = whitelist==null?"":whitelist;
		this.blacklist = blacklist==null?"":blacklist;
		this.readOnStartup = readOnStartup;
		this.suffix = suffix;
		try {
			existingFiles = new HashSet<String>();
			getFiles(path);
		} catch (IOException e) {
			LOGGER.error("Unable to get files in directory", e);
		}
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public int getTagLevel() {
		return tagLevel;
	}
	public void setTagLevel(int tagLevel) {
		this.tagLevel = tagLevel;
	}
	public String getWhitelist() {
		return whitelist;
	}
	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist;
	}
	public String getBlacklist() {
		return blacklist;
	}
	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist;
	}
    public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public Set<String> getExistingFiles() {
		return existingFiles;
	}
	public void setExistingFiles(Set<String> existingFiles) {
		this.existingFiles = existingFiles;
	}	
	public boolean isReadOnStartup() {
		return readOnStartup;
	}
	public void setReadOnStartup(boolean readOnStartup) {
		this.readOnStartup = readOnStartup;
	}
	/**
	 * Given a file, checks if the file is in the whitelist, so have to proccess or in the blacklist
	 * so have to discard
	 * @param file
	 * @return
	 */
	public boolean haveToProccess(String file){
		// Los ficheros que finalizan con el sufijo (.finished por defecto) no se tratan
		if (getWhitelist().isEmpty() && getBlacklist().isEmpty()){
			// Si las dos listas estan vacias notificamos
			return true;    							
		} else {
			// En caso contrario
			// Comprobamos si esta en la blacklist
			if (!getWhitelist().isEmpty() && match(getWhitelist(), file)){
				LOGGER.debug("Whitelisted. Go on");
				return true;        							//break;
			} else if (!getBlacklist().isEmpty() && !match(getBlacklist(), file)) {
				LOGGER.debug("Not in blacklisted. Go on");
				return true;
			}
		}
		
		return false;
		
	}
	
	private static boolean match(String patterns, String string) {
    	
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
	
	private void getFiles(String path) throws IOException {
		
		Path start = FileSystems.getDefault().getPath(path);
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				if (haveToProccess(file.toString())) existingFiles.add(file.toString());
				
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
