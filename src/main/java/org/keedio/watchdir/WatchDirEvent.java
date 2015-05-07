package org.keedio.watchdir;

/**
 * 
 * Events to be launch between components
 *
 */
public class WatchDirEvent {

	private String type;
	private String path;
	private WatchDirFileSet set;
	
	public WatchDirEvent(String path, String type, WatchDirFileSet set) {
		this.type = type;
		this.path = path;
		this.set = set;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public WatchDirFileSet getSet() {
		return set;
	}

	public void setSet(WatchDirFileSet set) {
		this.set = set;
	}
	
}
